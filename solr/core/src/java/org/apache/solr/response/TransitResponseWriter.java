/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.response;

/**
 * Types Left TODO:
 *  - ResultContext
 *  - DocList
 *  - BytesRef (read the docs carefully, UTF8 vs. UTF16 concerns here)
 *  - Mangling of NamedList
 */

import java.io.IOException;

import org.apache.lucene.index.StoredDocument;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformContext;
import org.apache.solr.search.ReturnFields;

import java.io.ByteArrayOutputStream;

import com.cognitect.transit.TransitFactory;
import com.cognitect.transit.Writer;

import java.util.ArrayList;
import java.util.HashMap;

public class TransitResponseWriter implements QueryResponseWriter {
  static String CONTENT_TYPE = "application/transit+json";

  @Override
  public void init(NamedList namedList) {
  }

  @Override
  public void write(java.io.Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Writer transit = TransitFactory.writer(TransitFactory.Format.JSON, baos);
    transit.write(transformObject(rsp.getValues(), req, rsp));
    writer.write(baos.toString());
  }

  @Override
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    return CONTENT_TYPE;
  }

  private HashMap<String, Object> transformNamedList(NamedList val, SolrQueryRequest request, SolrQueryResponse response) throws IOException {
    HashMap<String, Object> returnVal = new HashMap<>();
    int size = val.size();
    for (int i = 0; i < size; i++) {
      String key = val.getName(i);
      if (key == null) key = "";
      returnVal.put(key, transformObject(val.getVal(i), request, response));
    }
    return returnVal;
  }
  
  private HashMap<String, Object> transformSolrDocumentList(SolrDocumentList c, SolrQueryRequest request, SolrQueryResponse response) {
    HashMap<String, Object> returnVal = new HashMap<>();
    returnVal.put("numFound", c.getNumFound());
    returnVal.put("start", c.getStart());
    if (c.getMaxScore() != null) {
      returnVal.put("maxScore", c.getMaxScore());
    }
    ArrayList<HashMap<String, Object>> documents = new ArrayList<>();
    for (SolrDocument doc : c) {
      documents.add(transformSolrDocument(doc, request, response));
    }
    returnVal.put("documents", documents);
    return returnVal;
  }
  
  private HashMap<String, Object> transformSolrDocument(SolrDocument c, SolrQueryRequest request, SolrQueryResponse response) {
    ReturnFields returnFields = response.getReturnFields();
    HashMap<String, Object> docHash = new HashMap<>();
    for (String fname : c.getFieldNames()) {
      if (!returnFields.wantsField(fname)) {
        continue;
      }
      docHash.put(fname, c.getFieldValue(fname));
    }
    return docHash;
  }
  
  private final SolrDocument toSolrDocument( StoredDocument doc, SolrQueryRequest req ) 
  {
    return ResponseWriterUtil.toSolrDocument(doc, req.getSchema());
  }
  
  private Object transformObject(Object c, SolrQueryRequest request, SolrQueryResponse response) throws IOException {
    if (c instanceof SolrDocumentList) {
      return transformSolrDocumentList((SolrDocumentList) c, request, response);
    }
    if (c instanceof NamedList) {
      return transformNamedList((NamedList)c, request, response);
    } 
    if (c instanceof StoredDocument) {
      SolrDocument doc = toSolrDocument( (StoredDocument)c, request );
      ReturnFields returnFields = response.getReturnFields();
      DocTransformer transformer = returnFields.getTransformer();
      if( transformer != null ) {
        TransformContext context = new TransformContext();
        context.req = request;
        transformer.setContext(context);
        transformer.transform(doc, -1);
      }
      return transformSolrDocument(doc, request, response);
    }
    
    return c;
  }

}
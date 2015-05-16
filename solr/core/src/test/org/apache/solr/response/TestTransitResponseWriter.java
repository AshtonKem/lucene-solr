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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.ReturnFields;
import org.apache.solr.search.SolrReturnFields;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cognitect.transit.TransitFactory;
import com.cognitect.transit.Writer;


/**
 * Basic TransitResponseWriter tests based on JSONWriterTest
 *
 * Be warned, a few of these tests are ordering sensitive.
 */
public class TestTransitResponseWriter extends SolrTestCaseJ4 {
  private String writeTransit(Object data) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Writer writer = TransitFactory.writer(TransitFactory.Format.JSON, baos);
    writer.write(data);
    return baos.toString();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml","schema.xml");
  }


  @Test
  public void testContentType() throws IOException {
    SolrQueryRequest req = req("dummy");
    SolrQueryResponse rsp = new SolrQueryResponse();
    QueryResponseWriter w = new TransitResponseWriter();

    assertEquals("application/transit+json", w.getContentType(req, rsp));
  }




  @Test
  public void testSimple() throws IOException {
    SolrQueryRequest req = req("dummy");
    SolrQueryResponse rsp = new SolrQueryResponse();
    QueryResponseWriter w = new TransitResponseWriter();

    StringWriter buf = new StringWriter();
    rsp.add("data1", "hello");
    rsp.add("data2", 42);
    rsp.add("data3", true);
    rsp.add("data4", 45.0);
    w.write(buf, req, rsp);
    HashMap data = new HashMap<String, Object>();
    data.put("data1", "hello");
    data.put("data2", 42);
    data.put("data3", true);
    data.put("data4", 45.0);
    String result = writeTransit(data);
    assertEquals(result,
                 buf.toString());
    req.close();
  }

  
  @Test
  public void testComposite() throws IOException {
    SolrQueryRequest req = req("dummy");
    SolrQueryResponse rsp = new SolrQueryResponse();
    QueryResponseWriter w = new TransitResponseWriter();

    StringWriter buf = new StringWriter();
    ArrayList<String> l = new ArrayList<>();
    l.add("one");
    l.add("two");
    rsp.add("list", l);
    w.write(buf, req, rsp);
    HashMap data = new HashMap<String, Object>();
    data.put("list", l);
    String result = writeTransit(data);
    assertEquals(result,
                 buf.toString());
    req.close();
  }
  
  @Test
  public void testResponseDocuments() throws IOException {
    SolrQueryRequest req = req(CommonParams.WT,"json",
        CommonParams.FL,"id,score");
    SolrQueryResponse rsp = new SolrQueryResponse();
    TransitResponseWriter w = new TransitResponseWriter();
    
    ReturnFields returnFields = new SolrReturnFields(req);
    rsp.setReturnFields(returnFields);
    
    StringWriter buf = new StringWriter();
    
    SolrDocument solrDoc = new SolrDocument();
    solrDoc.addField("id", "1");
    solrDoc.addField("subject", "hello2");
    solrDoc.addField("title", "hello3");
    solrDoc.addField("score", "0.7");
    
    SolrDocumentList list = new SolrDocumentList();
    list.setNumFound(1);
    list.setStart(0);
    list.setMaxScore(0.7f);
    list.add(solrDoc);
    
    rsp.add("response", list);
    
    w.write(buf, req, rsp);
    String result = buf.toString();
    HashMap<String, Object> data = new HashMap<>();
    HashMap<String, Object> response = new HashMap<>();
    HashMap<String, Object> document = new HashMap<>();
    ArrayList docs = new ArrayList();
    
    document.put("id", "1");
    document.put("score", "0.7");
    docs.add(document);
    response.put("start", 0);
    response.put("numFound", 1);
    response.put("documents", docs);
    
    response.put("maxScore", 0.7);
    data.put("response", response);
    assertEquals( writeTransit(data), result);
  }
  
  
  @Test
  public void testMultiValuedField() throws IOException {
    SolrQueryRequest req = req(CommonParams.WT,"json",
        CommonParams.FL,"id,tags");
    SolrQueryResponse rsp = new SolrQueryResponse();
    TransitResponseWriter w = new TransitResponseWriter();
    
    ReturnFields returnFields = new SolrReturnFields(req);
    rsp.setReturnFields(returnFields);
    
    StringWriter buf = new StringWriter();
    
    SolrDocument solrDoc = new SolrDocument();
    solrDoc.addField("id", "1");
    solrDoc.addField("tags", "one");
    solrDoc.addField("tags", "two");
    
    SolrDocumentList list = new SolrDocumentList();
    list.setNumFound(1);
    list.setStart(0);
    list.add(solrDoc);
    
    rsp.add("response", list);
    
    w.write(buf, req, rsp);
    String result = buf.toString();
    HashMap<String, Object> data = new HashMap<>();
    HashMap<String, Object> response = new HashMap<>();
    HashMap<String, Object> document = new HashMap<>();
    ArrayList docs = new ArrayList();
    
    document.put("id", "1");
    Object[] tags = {"one", "two"};
    document.put("tags", tags);
    docs.add(document);
    response.put("start", 0);
    response.put("numFound", 1);
    response.put("documents", docs);
    data.put("response", response);
    String expected = writeTransit(data);
    assertEquals( expected, result);
  }
}

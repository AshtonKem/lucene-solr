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

import java.io.IOException;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import java.io.ByteArrayOutputStream;
import com.cognitect.transit.TransitFactory;
import com.cognitect.transit.Writer;
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
    transit.write(getData(rsp.getValues()));
    writer.write(baos.toString());
  }

  @Override
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    return CONTENT_TYPE;
  }

  private HashMap<String, Object> getData(NamedList val) {
    HashMap<String, Object> returnVal = new HashMap<>();
    int size = val.size();
    for (int i = 0; i < size; i++) {
      String key = val.getName(i);
      if (key == null) key = "";
      returnVal.put(key, transformObject(val.getVal(i)));
    }
    return returnVal;
  }
  
  private Object transformObject(Object c) {
    
    return c;
  }

}
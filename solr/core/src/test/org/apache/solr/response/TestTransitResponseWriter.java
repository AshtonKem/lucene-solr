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
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cognitect.transit.TransitFactory;
import com.cognitect.transit.Writer;


/**
 * Basic PHPS tests based on JSONWriterTest
 *
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


  // @Test
  // public void testSolrDocuments() throws IOException {
  //   SolrQueryRequest req = req("q","*:*");
  //   SolrQueryResponse rsp = new SolrQueryResponse();
  //   QueryResponseWriter w = new PHPSerializedResponseWriter();
  //   StringWriter buf = new StringWriter();

  //   SolrDocument d = new SolrDocument();

  //   SolrDocument d1 = d;
  //   d.addField("id","1");
  //   d.addField("data1","hello");
  //   d.addField("data2",42);
  //   d.addField("data3",true);

  //   // multivalued fields:

  //   // extremely odd edge case: value is a map

  //   // we use LinkedHashMap because we are doing a string comparison
  //   // later and we need predictible ordering
  //   LinkedHashMap<String,String> nl = new LinkedHashMap<>();
  //   nl.put("data4.1", "hashmap");
  //   nl.put("data4.2", "hello");
  //   d.addField("data4",nl);
  //   // array value
  //   d.addField("data5",Arrays.asList("data5.1", "data5.2", "data5.3"));

  //   // adding one more document to test array indexes
  //   d = new SolrDocument();
  //   SolrDocument d2 = d;
  //   d.addField("id","2");

  //   SolrDocumentList sdl = new SolrDocumentList();
  //   sdl.add(d1);
  //   sdl.add(d2);
  //   rsp.add("response", sdl);

  //   w.write(buf, req, rsp);
  //   assertEquals("a:1:{s:8:\"response\";a:3:{s:8:\"numFound\";i:0;s:5:\"start\";i:0;s:4:\"docs\";a:2:{i:0;a:6:{s:2:\"id\";s:1:\"1\";s:5:\"data1\";s:5:\"hello\";s:5:\"data2\";i:42;s:5:\"data3\";b:1;s:5:\"data4\";a:2:{s:7:\"data4.1\";s:7:\"hashmap\";s:7:\"data4.2\";s:5:\"hello\";}s:5:\"data5\";a:3:{i:0;s:7:\"data5.1\";i:1;s:7:\"data5.2\";i:2;s:7:\"data5.3\";}}i:1;a:1:{s:2:\"id\";s:1:\"2\";}}}}",
  //                buf.toString());
  //   req.close();
  // }

}

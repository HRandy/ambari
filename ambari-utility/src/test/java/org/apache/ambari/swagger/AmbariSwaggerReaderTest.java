/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.swagger;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;

public class AmbariSwaggerReaderTest {


  /**
   * Test the {@link AmbariSwaggerReader#joinPaths(String, String...)} method
   */
  @Test
  public void testJoinPaths() {
    assertEquals("/toplevel/nested/{param}/list",
        AmbariSwaggerReader.joinPaths("", "/", "/", "", "toplevel", "/nested/", "/{param}", "list"));
    assertEquals("/toplevel/nested/{param}/list",
        AmbariSwaggerReader.joinPaths("/", "toplevel", "", "/nested/", "/", "/{param}", "list", ""));
  }

  /**
   * Test the basic case: one top level API and one nested API, each with one operation
   */
  @Test
  public void swaggerBasicCase() {
    AmbariSwaggerReader asr = new AmbariSwaggerReader(null, createMock(Log.class));
    Swagger swagger = asr.read(ImmutableSet.of(TopLevelAPI.class, NestedAPI.class));
    assertEquals(ImmutableSet.of("/toplevel/top", "/toplevel/{param}/nested/list"),
        swagger.getPaths().keySet());
    assertPathParamsExist(swagger, "/toplevel/{param}/nested/list", "param");
  }

  /**
   * Test conflicting nested API's (the same API's are returned from different top level API's).
   * In this case the nested API should be associated to the first processed top level API.
   */
  @Test
  public void swaggerConflictingNestedApis() {
    AmbariSwaggerReader asr = new AmbariSwaggerReader(null, createMock(Log.class));
    ListOrderedSet classes = ListOrderedSet.decorate(
        Lists.newArrayList(TopLevelAPI.class, AnotherTopLevelAPI.class, NestedAPI.class));
    Swagger swagger = asr.read(classes);
    assertEquals(
        ImmutableSet.of("/toplevel/top", "/toplevel/{param}/nested/list", "/toplevel2/anotherTop"),
        swagger.getPaths().keySet());
    assertPathParamsExist(swagger, "/toplevel/{param}/nested/list", "param");
  }

  /**
   * If an API is both top level (the class has a @Path annotation) and nested (class is a return type of an
   * API operation) then it should be treated as top level.
   */
  @Test
  public void swaggerApiThatIsBothTopLevelAndNestedIsCountedAsTopLevel() {
    AmbariSwaggerReader asr = new AmbariSwaggerReader(null, createMock(Log.class));
    Swagger swagger = asr.read(ImmutableSet.of(YetAnotherTopLevelAPI.class, NestedAndTopLevelAPI.class));
    assertEquals(ImmutableSet.of("/toplevel3/yetAnotherTop", "/canBeReachedFromTopToo/list"),
        swagger.getPaths().keySet());
  }


  /**
   * Verify that the top level API's path parameters are transferred to the nested API.
   */
  private static void assertPathParamsExist(Swagger swagger, String path, String... expectedPathParams) {
    List<Parameter> parameters = swagger.getPath(path).getGet().getParameters();
    assertNotNull("No path parameters for path: " + path, parameters);
    Set<String> pathParamNames = new HashSet<>();
    for (Parameter param: parameters) {
      if (param instanceof PathParameter) {
        pathParamNames.add(param.getName());
      }
    }
    Set<String> missingPathParams = Sets.difference(ImmutableSet.copyOf(expectedPathParams), pathParamNames);
    assertTrue("Expected path params for [" + path + "] are missing: " + missingPathParams, missingPathParams.isEmpty());
  }

}

@Path("/toplevel")
@Api(value = "Top Level", description = "A top level API")
abstract class TopLevelAPI {

  @GET
  @Path("/top")
  @ApiOperation(value = "list")
  public abstract Response getList();

  @Path("{param}/nested")
  public abstract NestedAPI getNested(@ApiParam @PathParam(value = "param") String param);
}

@Path("/toplevel2")
@Api(value = "Top Level 2", description = "Another top level API")
abstract class AnotherTopLevelAPI {

  @GET
  @Path("/anotherTop")
  @ApiOperation(value = "list")
  public abstract Response getList();

  @Path("{param}/anotherNested")
  public abstract NestedAPI getSecondNested(@ApiParam @PathParam(value = "param") String param);

}

@Path("/toplevel3")
@Api(value = "Top Level 3", description = "Yet another top level API")
abstract class YetAnotherTopLevelAPI {

  @GET
  @Path("/yetAnotherTop")
  @ApiOperation(value = "list")
  public abstract Response getList();

  @Path("{param}/nested")
  public abstract NestedAPI getFirstNested(@ApiParam @PathParam(value = "param") String param);

}

@Api(value = "Nested", description = "A nested API")
abstract class NestedAPI {

  @GET
  @Path("/list")
  @ApiOperation(value = "list")
  public abstract Response getList();

}

@Path("/canBeReachedFromTopToo")
@Api(value = "Nested and Top Level", description = "An API that is both nested and top level")
abstract class NestedAndTopLevelAPI {

  @GET
  @Path("/list")
  @ApiOperation(value = "list")
  public abstract Response getList();

}

/**
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
package org.apache.ambari.server.events;

import java.util.Map;
import java.util.TreeMap;

import org.apache.ambari.server.agent.stomp.dto.Hashable;
import org.apache.ambari.server.agent.stomp.dto.MetadataCluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetadataUpdateEvent extends AmbariUpdateEvent implements Hashable {

  private String hash;

  @JsonProperty("clusters")
  private TreeMap<String, MetadataCluster> metadataClusters = new TreeMap<>();

  public MetadataUpdateEvent(TreeMap<String, MetadataCluster> metadataClusters) {
    super(Type.METADATA);
    this.metadataClusters = metadataClusters;
  }

  public Map<String, MetadataCluster> getMetadataClusters() {
    return metadataClusters;
  }

  public void setMetadataClusters(TreeMap<String, MetadataCluster> metadataClusters) {
    this.metadataClusters = metadataClusters;
  }

  @Override
  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public static MetadataUpdateEvent emptyUpdate() {
    return new MetadataUpdateEvent(null);
  }
}
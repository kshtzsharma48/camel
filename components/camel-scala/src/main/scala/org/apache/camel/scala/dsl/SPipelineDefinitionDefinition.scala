/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.scala.dsl

import builder.RouteBuilder
import org.apache.camel.model.PipelineDefinition

/**
 * Scala enrichment for Camel's PipelineDefinition
 */
case class SPipelineDefinition(override val target: PipelineDefinition)(implicit val builder: RouteBuilder) extends SAbstractDefinition[PipelineDefinition] {

  override def to(uris: String*) : SPipelineDefinition = {
    uris.length match {
      case 1 => target.to(uris(0))
      case _ => {
        for (uri <- uris) this.to(uri)
      }
    }
    this
  }

  override def apply(block: => Unit) = wrap(super.apply(block))

  override def wrap(block: => Unit) = super.wrap(block).asInstanceOf[SPipelineDefinition]

}

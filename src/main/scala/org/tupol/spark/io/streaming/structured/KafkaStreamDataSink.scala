/*
MIT License

Copyright (c) 2018 Tupol (github.com/tupol)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package org.tupol.spark.io.streaming.structured

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.StreamingQuery
import org.tupol.spark.Logging
import org.tupol.spark.io.FormatType.Kafka
import org.tupol.spark.io.{ DataAwareSink, DataSink }
import org.tupol.utils.config.Configurator
import scalaz.ValidationNel

case class KafkaStreamDataSink(configuration: KafkaStreamDataSinkConfiguration)
  extends DataSink[KafkaStreamDataSinkConfiguration, StreamingQuery] with Logging {

  /** Try to write the data according to the given configuration and return the same data or a failure */
  override def write(data: DataFrame): StreamingQuery =
    GenericStreamDataSink(configuration.generic).write(data)
}

/** KafkaDataSink trait that is data aware, so it can perform a write call with no arguments */
case class KafkaStreamDataAwareSink(configuration: KafkaStreamDataSinkConfiguration, data: DataFrame)
  extends DataAwareSink[KafkaStreamDataSinkConfiguration, StreamingQuery] {
  override def sink: DataSink[KafkaStreamDataSinkConfiguration, StreamingQuery] = KafkaStreamDataSink(configuration)
}

case class KafkaStreamDataSinkConfiguration(
  private val kafkaBootstrapServers: String,
  private val genericConfig: GenericStreamDataSinkConfiguration,
  private val topic: Option[String] = None,
  private val checkpointLocation: Option[String] = None)
  extends FormatAwareStreamingSinkConfiguration {
  val format = Kafka
  private val options = genericConfig.options ++
    Map(
      "kafka.bootstrap.servers" -> Some(kafkaBootstrapServers),
      "topic" -> topic,
      "checkpointLocation" -> checkpointLocation)
    .collect { case (key, Some(value)) => (key, value) }
  val generic = genericConfig.copy(options = options)
  override def toString: String = generic.toString
}
object KafkaStreamDataSinkConfiguration extends Configurator[KafkaStreamDataSinkConfiguration] {
  import com.typesafe.config.Config
  import org.tupol.utils.config._
  import scalaz.syntax.applicative._

  def validationNel(config: Config): ValidationNel[Throwable, KafkaStreamDataSinkConfiguration] = {
    config.extract[String]("kafka.bootstrap.servers") |@|
      config.extract[GenericStreamDataSinkConfiguration] |@|
      config.extract[Option[String]]("topic") |@|
      config.extract[Option[String]]("checkpointLocation") apply
      KafkaStreamDataSinkConfiguration.apply
  }
}

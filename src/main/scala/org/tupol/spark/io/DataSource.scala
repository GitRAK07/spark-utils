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
package org.tupol.spark.io

import com.typesafe.config.Config
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.tupol.spark.io.sources.{ GenericSourceConfiguration, JdbcSourceConfiguration }
import org.tupol.utils.config.Configurator
import scalaz.ValidationNel

/** Common trait for reading a DataFrame from an external resource */
trait DataSource[Config <: DataSourceConfiguration] {
  /** `DataSource` configuration */
  def configuration: Config
  /** Read a `DataFrame` using the given configuration and the `spark` session available. */
  def read(implicit spark: SparkSession): DataFrame
}

/** Factory trait for DataSourceFactory */
trait DataSourceFactory {
  def apply[Config <: DataSourceConfiguration](configuration: Config): DataSource[Config]
}

/** Common marker trait for `DataSource` configuration that also knows the data format  */
trait FormatAwareDataSourceConfiguration extends DataSourceConfiguration with FormatAware

/** Factory for FormatAwareDataSourceConfiguration */
object FormatAwareDataSourceConfiguration extends Configurator[FormatAwareDataSourceConfiguration] {
  override def validationNel(config: Config): ValidationNel[Throwable, FormatAwareDataSourceConfiguration] =
    FileSourceConfiguration.validationNel(config) orElse
      JdbcSourceConfiguration.validationNel(config) orElse
      GenericSourceConfiguration.validationNel(config)
}
/** Common marker trait for `DataSource` configuration */
trait DataSourceConfiguration

case class DataSourceException(private val message: String = "", private val cause: Throwable = None.orNull)
  extends Exception(message, cause)


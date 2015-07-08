package services

import models.Identification
import models.Classification
import models.Class
import scala.concurrent.Future
import play.api.Play.current
import weka.classifiers.bayes.NaiveBayes
import scala.collection.JavaConversions._
import weka.core.DenseInstance
import weka.core.Attribute
import weka.core.Instances
import weka.core.converters.ConverterUtils.DataSource
import weka.filters.MultiFilter
import weka.filters.Filter
import weka.classifiers.Classifier
import weka.core.OptionHandler
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger

trait ClassificationService {
  var classifier: Classifier with OptionHandler = null

  var filter = new MultiFilter

  var instances: Instances = null

  var classList: Seq[String] = null

  def initialize() = {
    val classifierClass = current.configuration.getString("classifier.default").get
    val clazz = current.classloader.loadClass(classifierClass)
    classifier = clazz.newInstance().asInstanceOf[Classifier with OptionHandler]

    val options = current.configuration.getString("classifier.options").get.split(",")
    classifier.setOptions(options)

    val trainingFile = current.configuration.getString("classifier.trainingfile").get
    val source = new DataSource(trainingFile)
    val trainingSet = source.getDataSet

    // the last attribute is the class
    trainingSet.setClassIndex(trainingSet.numAttributes() - 1)

    // extract classes
    classList = trainingSet.classAttribute().enumerateValues().toList map (_.toString)

    filter.setInputFormat(trainingSet)
    val filteredTrainingSet = Filter.useFilter(trainingSet, filter)

    Logger.debug("Building classifier.")
    classifier.buildClassifier(filteredTrainingSet);
    Logger.debug("Done building classifier.")

    instances = filteredTrainingSet
  }

  /**
   * Use with caution. Could be worse as long as the feature attributes are not adjusted.
   */
  private def setFilterOptions() = {
    val filterOptions = current.configuration.getString("classifier.filter.args").get.split(",")
    filter.setOptions(filterOptions)
  }

  def classify(identification: Identification): Future[Classification] = {
    identification.featureVector map { featureVector =>
      Future {
        val instance = new DenseInstance(featureVector.features.size + 1)
        Logger.debug(s"${featureVector.features}")

        featureVector.features.zipWithIndex foreach {
          case (feature, index) =>
            instance.setValue(index, feature.value)
        }

        instance.setDataset(instances)

        filter.input(instance)
        filter.batchFinished()
        val filteredInstance = filter.output

        val classes = classifier.distributionForInstance(filteredInstance)

        val result = classes.zipWithIndex map {
          case (probability, index) =>
            `Class`(classList(index), probability)
        }

        Classification(result.sortBy(_.probability).reverse.take(3))
      }
    } getOrElse {
      Future.failed(new RuntimeException(s"Could not classify identification $identification"))
    }
  }
}

object ClassificationService extends ClassificationService
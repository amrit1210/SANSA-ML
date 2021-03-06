package net.sansa_stack.ml.spark.similarity

import com.holdenkarau.spark.testing.DataFrameSuiteBase
import net.sansa_stack.ml.spark.similarity.similarityEstimationModels.{BatetModel, BraunBlanquetModel, DiceModel, JaccardModel, MinHashModel, OchiaiModel, SimpsonModel, TverskyModel}
import net.sansa_stack.ml.spark.utils.{FeatureExtractorModel, SimilarityExperimentMetaGraphFactory}
import org.apache.jena.riot.Lang
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.scalatest.FunSuite
import net.sansa_stack.rdf.spark.io._
import org.apache.jena.graph
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel, MinHashLSH, MinHashLSHModel}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.types.DataTypes

class similarityUnitTest extends FunSuite with DataFrameSuiteBase {

  // define inputpath if it is not parameter
  val inputPath = "src/test/resources/similarity/movie.nt"
  // println(inputPath)

  test("Read in Data and create a Feature DataFrame") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)
    triplesDf.count()

    val modesToTest = List("an", "at", "as", "or")

    for (mode <- modesToTest) {
      // println("feature extraction with mode: " + mode)
      val featureExtractorModel = new FeatureExtractorModel()
        .setMode(mode)
      val extractedFeaturesDataFrame = featureExtractorModel
        .transform(triplesDf)
        .filter(t => t.getAs[String]("uri").startsWith("m"))
      extractedFeaturesDataFrame.count()
    }
  }

  test("Test Batet Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // Braun Blanquet Similarity
    val braunBlanquetModel: BraunBlanquetModel = new BraunBlanquetModel()
      .setInputCol("vectorizedFeatures")
    braunBlanquetModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10).count()
    braunBlanquetModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5).count()
  }

  test("Test Braun Blanquet Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // Braun Blanquet Similarity
    val braunBlanquetModel: BraunBlanquetModel = new BraunBlanquetModel()
      .setInputCol("vectorizedFeatures")
    braunBlanquetModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10).count()
    braunBlanquetModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5).count()
  }

  test("Test Dice Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // Dice Similarity
    val diceModel: DiceModel = new DiceModel()
      .setInputCol("vectorizedFeatures")
    diceModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10).count()
    diceModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5).count()
  }

  test("Test Jaccard Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // Jaccard similarity
    val jaccardModel: JaccardModel = new JaccardModel()
      .setInputCol("vectorizedFeatures")
    jaccardModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10).count()
    jaccardModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5).count()
  }

  test("Test MinHash Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // minHash similarity estimation
    val minHashModel: MinHashModel = new MinHashModel()
      .setInputCol("vectorizedFeatures")
      .setNumHashTables(1)
    minHashModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10, "minHashDistance").count()
    minHashModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, 0.8, "distance").count()
  }

  test("Test Ochiai Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // Ochiai Similarity
    val ochiaiModel: OchiaiModel = new OchiaiModel()
      .setInputCol("vectorizedFeatures")
    ochiaiModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10).count()
    ochiaiModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5).count()
  }

  test("Test Simpson Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // Simpson Similarity
    val simpsonModel: SimpsonModel = new SimpsonModel()
      .setInputCol("vectorizedFeatures")
    simpsonModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10).count()
    simpsonModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5).count()
  }

  test("Test Tversky Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // Tversky Similarity
    val tverskyModel: TverskyModel = new TverskyModel()
      .setInputCol("vectorizedFeatures")
      .setAlpha(1.0)
      .setBeta(1.0)
    tverskyModel.nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10).count()
    tverskyModel.similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5).count()
  }

  test("Test Metragraph creation after Tversky Similarity Module") {

    // read in data as Data`Frame
    val triplesDf: DataFrame = spark.read.rdf(Lang.NTRIPLES)(inputPath)

    /**
     * for next tests we need the count vectorized feature representation
     */
    // feature extraction
    val featureExtractorModel = new FeatureExtractorModel()
      .setMode("at")
    val extractedFeaturesDataFrame = featureExtractorModel
      .transform(triplesDf)
    // filter for relevant URIs e.g. only movies
    val filteredFeaturesDataFrame = extractedFeaturesDataFrame.filter(t => t.getAs[String]("uri").startsWith("m"))

    // count Vectorization
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("extractedFeatures")
      .setOutputCol("vectorizedFeatures")
      .fit(filteredFeaturesDataFrame)
    val tmpCvDf: DataFrame = cvModel.transform(filteredFeaturesDataFrame)
    val isNoneZeroVector = udf({ v: Vector => v.numNonzeros > 0 }, DataTypes.BooleanType)
    val countVectorizedFeaturesDataFrame: DataFrame = tmpCvDf.filter(isNoneZeroVector(col("vectorizedFeatures"))).select("uri", "vectorizedFeatures")

    // similarity Estimations Overview
    // for nearestNeighbors we need one key which is a Vector to search for NN
    val sample_key: Vector = countVectorizedFeaturesDataFrame.take(1)(0).getAs[Vector]("vectorizedFeatures")

    // create Metagraph information for e.g. MinHash
    val model = new MinHashModel()  // : JaccardModel = new JaccardModel()
      .setInputCol("vectorizedFeatures")
    val outputDf1: Dataset[_] = model
      .nearestNeighbors(countVectorizedFeaturesDataFrame, sample_key, 10) // .similarityJoin(countVectorizedFeaturesDataFrame, countVectorizedFeaturesDataFrame, threshold = 0.5)

    val metaGraphFactory = new SimilarityExperimentMetaGraphFactory()
    val metagraph: RDD[graph.Triple] = metaGraphFactory.createRdfOutput(
      outputDataset = outputDf1)(
      modelInformationEstimatorName = model.estimatorName, modelInformationEstimatorType = model.modelType, modelInformationMeasurementType = model.estimatorMeasureType)(
      inputDatasetNumbertOfTriples = triplesDf.count(), dataSetInformationFilePath = inputPath)
    metagraph.count() // foreach(println(_))
  }
}

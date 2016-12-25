package org.dotdi.DLSparkUtils.examples.word2vec

import org.slf4j.LoggerFactory
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer

// For using the Java collections
import scala.collection.JavaConversions._

object Product2Vec {

  val log = LoggerFactory.getLogger(classOf[Product2Vec]);

  def main(args: Array[String]): Unit = {
    // Gets Path to Text file
    val filePath = "/Users/dot/data_encrypted/red/lists/products3750000";

    log.info("Load & Vectorize Sentences....");

    // Strip white space before and after for each line
    val iter = new BasicLineIterator(filePath);
    // Split on white spaces in the line to get words
    val t = new DefaultTokenizerFactory();

    /*
       CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
       So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
       Additionally it forces lower case for all tokens.
     */
    t.setTokenPreProcessor(new CommonPreprocessor());

    log.info("Building model....");
    val vec = new Word2Vec.Builder()
      .minWordFrequency(5)
      .iterations(1)
      .layerSize(100)
      .seed(42)
      .windowSize(5)
      .iterate(iter)
      .tokenizerFactory(t)
      .build();

    log.info("Fitting Word2Vec model....");
    vec.fit();

    log.info("Writing word vectors to text file....");

    // Write word vectors to file
    WordVectorSerializer.writeWordVectors(vec, "/Users/dot/data_encrypted/product2vec/product2vec_scala.txt");

    // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
    log.info("Closest Words:");
    val lst = vec.wordsNearest("day", 10);
    println("10 Words closest to 'day': " + lst.mkString("; "));
  }

}

case class Product2Vec {

}

package uk.ac.ucl.cs.mr.statnlpbook.chapter

import ml.wolfe.nlp.Document
import scala.annotation.tailrec
import scala.util.Random

import uk.ac.ucl.cs.mr.statnlpbook.corpora.OHHLA

/**
 * @author riedel
 */
object LanguageModels {

  val random = new Random(0)
  type History = List[String]
  type Vocab = Set[String]

  def NoHistory = Vector.empty[String]

  def words(docs: Iterable[Document], padding: Int = 0) = {
    val content = docs flatMap (_.sentences flatMap (s => Vector.fill(padding)(PAD) ++ (s.tokens map (_.word))))
    content.toIndexedSeq
  }

  def injectOOVs(oov: String, words: Seq[String]) = {
    case class Result(vocab: Set[String], processed: List[String])
    def combine(result: Result, word: String) =
      if (result.vocab(word)) result.copy(processed = word :: result.processed)
      else Result(result.vocab + word, oov :: result.processed)
    val result = words.foldLeft(Result(Set.empty, Nil))(combine)
    result.processed.reverse.toIndexedSeq
  }

  def replaceOOVs(oov: String, vocab: Vocab, corpus: Seq[String]) =
    (corpus map (w => if (vocab(w)) w else oov)).toIndexedSeq


  @tailrec
  def replaceFirstOccurenceWithOOV(oov: String,
                                   toProcess: History,
                                   result: History = Nil,
                                   vocab: Vocab = Set(PAD)): History = {
    toProcess match {
      case Nil => result
      case head :: tail =>
        val (word, newVocab) = if (vocab(head)) (head, vocab) else (oov, vocab + head)
        replaceFirstOccurenceWithOOV(oov, tail, word :: result, newVocab)
    }
  }

  def padded(history: History, howMuch: Int = 5) = {
    val init = (0 until howMuch).toList map (_ => PAD)
    history ++ init
  }


  def filterByVocab(vocab: Vocab, oov: String, corpus: History) =
    corpus map (w => if (vocab(w)) w else oov)

  def OOV = "[OOV]"

  def PAD = "[PAD]"

  val lmRandom = new Random()

  def sampleCategorical(probs: IndexedSeq[Double]) = {
    val s = lmRandom.nextDouble()
    var i = 0
    var sum = 0.0
    while (i < probs.length && sum <= s) {
      sum += probs(i)
      i += 1
    }
    i - 1
  }

  private def revCross[T](args: List[List[T]], result: List[List[T]] = List(Nil)): List[List[T]] = {
    args match {
      case Nil => result
      case h :: t =>
        val appended = for (f <- h; r <- result) yield f :: r
        cross(t, appended)
    }
  }
  def cross[T](args: List[List[T]], result: List[List[T]] = List(Nil)): List[List[T]] = revCross(args.reverse,result)


  def main(args: Array[String]) {

    import LanguageModel._
    import OHHLA._

    List.fill(2)()

    println(sampleCategorical(Vector(0.0, 0.9, 0.1)))
    println(cross(List(List(1,2),List(3,4))))

    //when calculating perplexity and training a model, the input should always be padded
    //but OOV after padding creates: [PAD][OOV][OOV] ...
    //    val docs = JLive.allAlbums flatMap loadDir
    //    val (trainDocs, testDocs) = docs.splitAt(docs.length - 1)
    //    val train = replaceFirstOccurenceWithOOV(OOV, words(trainDocs).toList.reverse).reverse
    //    implicit val vocab = Vocab(train.distinct)
    //    val test = filterByVocab(vocab.words.toSet, OOV, words(testDocs).toList.reverse).reverse.toIndexedSeq
    //
    //    println(train.length)
    //
    //    //    println(words(OHHLA.loadAll(OHHLA.jLiveAllAbove)).take(100).mkString("\n"))
    //    //    println("---")
    //    //    println(test.take(10).mkString("\n"))
    //
    //    for (_ <- 0 until 1) {
    //      val lms = Seq(
    //        "vocabLM" -> uniform,
    //        "unigramLM" -> ngram(train, 1),
    //        "bigramLM" -> ngram(train, 2).laplace(1.0)
    //      )
    //
    //      for ((name, lm) <- lms) {
    //        println(name)
    //        println(lm.perplexity(test))
    //      }
    //    }

  }

}







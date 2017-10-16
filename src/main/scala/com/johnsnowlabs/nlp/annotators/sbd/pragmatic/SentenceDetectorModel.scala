package com.johnsnowlabs.nlp.annotators.sbd.pragmatic

import com.johnsnowlabs.nlp.annotators.sbd.Sentence
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel}
import org.apache.spark.ml.param.StringArrayParam
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}

/**
  * Annotator that detects sentence boundaries using any provided approach
  * @param uid internal constructor requirement for serialization of params
  * @@ model: Model to use for boundaries detection
  */
class SentenceDetectorModel(override val uid: String) extends AnnotatorModel[SentenceDetectorModel] {

  import com.johnsnowlabs.nlp.AnnotatorType._

  val model = new PragmaticMethod()

  val customBounds: StringArrayParam = new StringArrayParam(
    this,
    "customBounds",
    "characters used to explicitly mark sentence bounds"
  )

  def this() = this(Identifiable.randomUID("SENTENCE"))

  def setCustomBoundChars(value: Array[String]): this.type = set(customBounds, value)

  override val annotatorType: AnnotatorType = DOCUMENT

  override val requiredAnnotatorTypes: Array[AnnotatorType] = Array(DOCUMENT)

  setDefault(inputCols, Array(DOCUMENT.toString))

  /**
    * Uses the model interface to prepare the context and extract the boundaries
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return One to many annotation relationship depending on how many sentences there are in the document
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    annotations.flatMap(annotation => {
      val sentences: Seq[Sentence] = model.extractBounds(
        annotation.metadata(DOCUMENT),
        get(customBounds).getOrElse(Array.empty[String])
      )
      sentences.map(sentence => Annotation(
        this.annotatorType,
        sentence.begin,
        sentence.end,
        Map[String, String](annotatorType.toString -> sentence.content)
      ))
    })
  }
}

object SentenceDetectorModel extends DefaultParamsReadable[SentenceDetectorModel]
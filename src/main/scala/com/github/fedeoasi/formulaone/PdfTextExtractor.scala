package com.github.fedeoasi.formulaone

import java.io.File

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.{PDFTextStripper, TextPosition}

object PdfTextExtractor {
  def extractText(inputFile: File): Seq[PdfText] = {
    val document = PDDocument.load(inputFile)
    val textStripper = new CustomPdfTextStripper()
    textStripper.getText(document)
    val positions = textStripper.positions
    val chunks = groupIntoChunks(positions)
    document.close()
    chunks
  }

  def groupIntoChunks(positions: Seq[PdfText]): Seq[PdfText] = {
    val threshold = 10
    val (chunks, lastChunk) = positions.foldLeft((Seq.empty[PdfText], None: Option[PdfText])) { case ((result, prev), curr) =>
      prev match {
        case Some(p) =>
          if (Math.abs(curr.y - p.y) < threshold && Math.abs(curr.x - p.x) < threshold) {
            (result, Some(PdfText(p.value + curr.value, curr.x, curr.y)))
          } else {
            (p +: result, Some(curr))
          }
        case None => (result, Some(curr))
      }
    }
    val allChunks = lastChunk match {
      case Some(chunk) => chunk +: chunks
      case None => chunks
    }
    allChunks.map(c => c.copy(value = c.value.trim)).reverse
  }

  class CustomPdfTextStripper extends PDFTextStripper {
    var textPositions = Seq.empty[PdfText]

    override def processTextPosition(text: TextPosition): Unit = {
      textPositions = PdfText(text.getUnicode, text.getX, text.getY) +: textPositions
      super.processTextPosition(text)
    }

    def positions: Seq[PdfText] = textPositions.reverse
  }
}

case class PdfText(value: String, x: Float, y: Float)
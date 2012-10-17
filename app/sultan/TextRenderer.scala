package sultan

import org.pegdown.PegDownProcessor

object TextRenderer {

  val renderer = new PegDownProcessor

  def render(markdown: Option[String]): String = {
    markdown.map({ m => renderer.markdownToHtml(m) }).getOrElse("")
  }
}
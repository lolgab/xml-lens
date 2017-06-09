package net.michalsitko.xml.optics

import monocle.{Lens, Optional, Prism, Traversal}
import net.michalsitko.xml.entities._

import scalaz.Applicative

object Optics {
  import scalaz.std.list._
  import scalaz.syntax.traverse._
  import scalaz.syntax.applicative._


  def deep(label: String): Traversal[LabeledElement, Element] = deep(ResolvedName.unprefixed(label))

  // TODO: test laws (20 min)
  // TODO: implement other lenses and check if most popular use cases case covered (3 h)
  def deep(label: ResolvedName): Traversal[LabeledElement, Element] = new Traversal[LabeledElement, Element] {
    override final def modifyF[F[_]](f: (Element) => F[Element])(from: LabeledElement)(implicit F: Applicative[F]): F[LabeledElement] = {
      val tmp = from.element.children.collect {
        // TODO: equals for ResolvedName should return true for different prefixes but same fields otherwise
        case el: LabeledElement if el.label == label =>
          val mapped = f(el.element)
          F.map(mapped)(mappedDetail => LabeledElement(label, mappedDetail).asInstanceOf[Node])
        case anythingElse: Node =>
          F.pure(anythingElse)
      }.toList

      F.map(F.sequence(tmp)){ elements =>
        from.copy(element = from.element.copy(children = elements))
      }
    }
  }

  def deeper(label: String): Traversal[Element, Element] = deeper(ResolvedName.unprefixed(label))

  def deeper(label: ResolvedName): Traversal[Element, Element] = new Traversal[Element, Element] {
    override final def modifyF[F[_]](f: (Element) => F[Element])(from: Element)(implicit F: Applicative[F]): F[Element] = {
      val tmp = from.children.collect {
        // TODO: equals for ResolvedName should return true for different prefixes but same fields otherwise
        case el: LabeledElement if el.label == label =>
          val mapped = f(el.element)
          F.map(mapped)(mappedDetail => LabeledElement(label, mappedDetail).asInstanceOf[Node])
        case anythingElse =>
          F.pure(anythingElse)
      }.toList

      F.map(F.sequence(tmp)){ elements =>
        from.copy(children = elements)
      }
    }
  }

  val hasTextOnly: Optional[Element, String] = Optional[Element, String] { el =>
    if(el.children.size == 1) {
      el.children.head match {
        case Text(txt) => Some(txt)
        case _ => None
      }
    } else {
      None
    }
  }(newText => from => from.copy(children = List(Text(newText))))

//  val firstText: Lens[Element, String] =

}
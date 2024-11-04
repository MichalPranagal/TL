sealed trait Tree:
  override def toString: String

case class Node(children: List[Tree]) extends Tree:
  override def toString: String = children.mkString("(", " ", ")")

case class ID(name: String) extends Tree:
  override def toString: String = name

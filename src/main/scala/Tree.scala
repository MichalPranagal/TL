sealed trait Tree:
  override def toString: String

case class Node(children: List[Tree]) extends Tree:
  override def toString: String = children.mkString("(", " ", ")")

case class ID(name: String) extends Tree:
  override def toString: String = name

object ID:
  private val VALID_ID_REGEX = "[a-zA-Z0-9]+"
  def isValidName(name: String): Boolean = name matches VALID_ID_REGEX

object Tree:
  private val TOKENS_SPLIT_REGEX = "\\s+|\\b|(?<=[()])"

  def parse(treeStr: String): Either[String, Tree] =
    val tokens = treeStr.split(TOKENS_SPLIT_REGEX).filter(_.nonEmpty).toList
    parseTokens(tokens)
      .flatMap { (tree, remaining) => remaining match
        case Nil => Right(tree)
        case _ => Left(s"Unexpected token: '${remaining.head}'")
      }

  private def parseTokens(tokens: List[String]): Either[String, (Tree, List[String])] = tokens match
    case Nil => Left("Unexpected end of input")
    case "(" :: rest =>
      parseNode(rest)
        .map { (children, rest2) => (Node(children), rest2) }
    case name :: rest if ID isValidName name =>
      Right(ID(name), rest)
    case token :: _ =>
      Left(s"Unexpected token: '$token'")

  private def parseNode(tokens: List[String]): Either[String, (List[Tree], List[String])] = tokens match
    case ")" :: rest => Right(Nil, rest)
    case _ =>
      parseTokens(tokens)
        .flatMap { (child, rest2) =>
          parseNode(rest2)
            .map { (children, rest3) => (child :: children, rest3) }
        }


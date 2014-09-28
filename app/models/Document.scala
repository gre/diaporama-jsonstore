package models

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.Play.current
import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._
import reactivemongo.bson.Macros.Annotations.Key
import scala.concurrent.Future

case class Document(
  @Key("_id") id: Document.ID,
  json: JsObject
)
object Document {

  class ID(val intern: String) extends AnyVal
  object ID {
    def apply(str: String): ID = new ID(str)
    def generate: ID = ID(BSONObjectID.generate.stringify)
    implicit val bsonHandler = new BSONHandler[BSONString, ID] {
      def read(bson: BSONString) = ID(bson.value)
      def write(id: ID) = new BSONString(id.intern)
    }
  }

  def db = ReactiveMongoPlugin.db
  def collection = db("jsons")(BSONCollectionProducer)

  implicit val bsonWriter = new BSONDocumentWriter[Document] {
    def write(document: Document) = BSONDocument(
      "_id" -> document.id,
      "json" -> document.json
    )
  }
  implicit val bsonReader = new BSONDocumentReader[Document] {
    def read(buffer: BSONDocument) = Document(
      id = buffer.getAs[ID]("_id").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field _id: ${BSONDocument.pretty(buffer)}")
      },
      json = buffer.getAs[JsObject]("json").getOrElse {
        throw new java.lang.RuntimeException(s"Missing field json: ${BSONDocument.pretty(buffer)}")
      }
    )
  }

  def store(json: JsObject): Future[Document] = {
    val doc = Document(ID.generate, json)
    collection.insert(doc) map { _ => doc }
  }

  def update(id: ID, json: JsObject): Future[Option[Document]] = {
    collection.update(
      BSONDocument("_id" -> id),
      BSONDocument("$set" -> BSONDocument("json" -> json))
    ) map {
      case le if le.updated == 0 => None
      case _ => Some(Document(id, json))
    }
  }

  def get(id: ID): Future[Option[Document]] = {
    collection.find(BSONDocument("_id" -> id)).cursor[Document].headOption
  }

}

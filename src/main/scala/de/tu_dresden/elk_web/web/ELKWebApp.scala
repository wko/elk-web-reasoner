package de.tu_dresden.elk_web.web

import java.io.File

import com.google.gson.Gson
import de.tu_dresden.el2db.{DBParams, DatabaseManager, DatabaseModel}
import de.tu_dresden.elk_web.web.Command.Command
import org.apache.log4j.{Level, LogManager}
import org.phenoscape.scowl._
import org.scalatra._
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport
import org.scalatra.scalate.ScalateSupport
import org.semanticweb.owlapi.apibinding.OWLManager

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object Command extends Enumeration {
  type Command = Value
  val getBottomClassNode,
    getEquivalentClasses,
    getInstances,
    getSubClasses,
    getSuperClasses,
    getTopClassNode,
    getTypes,
    getUnsatisfiableClasses,
    isConsistent,
    isSatisfiable,
    getLabelForClass,
    getLabelForObjectProperty,
    Unknown = Value

  def withNameWithDefault(name: String): Value =
    values.find(_.toString.toLowerCase == name.toLowerCase()).getOrElse(Unknown)
}

case class FormInput(command: String, data: String, options: Option[String]) {
  val cmd:Command = Command.withNameWithDefault(command)
  val opts:Map[String, String] = options match {
    case Some(o) => {
      val pattern = """\W*(\w+)[^:]*:\W*(\w+)""".r
      var mapBuilder: mutable.Map[String, String] = mutable.Map.empty
      pattern.findAllIn(o).matchData foreach { m =>
        mapBuilder.+=((m.group(1), m.group(2)))
      }

      mapBuilder.toMap
    }
    case None => Map.empty
  }
}



class ELKWebApp extends ScalatraServlet with FormSupport with I18nSupport with ScalateSupport {
  LogManager.getRootLogger.setLevel(Level.DEBUG)
  val logger =  LogManager.getLogger(getClass)

  val ONTOLOGY_PATH:String = {
    sys.env.get("ONTOLOGY_PATH") match {
      case Some(path) => path
      case None => throw new Error("Please set ENV VAR 'ONTOLOGY_PATH' to point to the ontology file in ofn format.")
    }
  }
  var lastInput = FormInput("Unknown","",None)
  /*var helper: Future[OntologyHelper] = {
    val h = Future({
      logger.info("Loading ontology..")
      new OntologyHelper(new File(ONTOLOGY_PATH))
    })
    h.map(h1 => { logger.info("Precomputing Inferences..")
      h1.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)})
    h
  }*/
  var dbmodel: Future[DatabaseModel] = {

    val user = sys.env.get("DB_USER") match {
      case Some(pw) => pw
      case None => "postgres"
    }

    val password = sys.env.get("DB_PASSWORD") match {
      case Some(pw) => pw
      case None => ""
    }

    val host = sys.env.get("DB_HOST") match {
      case Some(pw) => pw
      case None => "localhost"
    }

    val port = sys.env.get("DB_PORT") match {
      case Some(pw) => pw
      case None => "5434"
    }
    def string2sql(s: String): String = "s" + s.filterNot("-_,;.?".contains(_)).toLowerCase


    val f = new File(ONTOLOGY_PATH)
    val manager = OWLManager.createOWLOntologyManager
    val ontology = manager.loadOntologyFromOntologyDocument(f)
    //logger.info("Loading ontology..")
    //val h = OntologyHelper.createOntologyHelper(f, None, false)
    val params = DBParams(s"jdbc:postgresql://${host}:${port}", string2sql(f.getName), user, password)
    val dbmanager = DatabaseManager.getManager(params)
    val model = new DatabaseModel(dbmanager)
    /*if (!model.isInitialized(ontology)) {
      logger.info("Model is not initialized..")
      logger.info(s"Saving Model to Database..")
      val infOnt = OntologyUtils.classifyEL(ontology)
      model.saveToDatabase(infOnt)
      logger.info("Model successfully saved to Database..")
    }*/
    Future(model)
  }
  val title:String = "ELK Web Interface"

  val input_form = mapping(
    "command"        -> label("commandString", text(required)),
    "data"        -> label("data", text(required)),
    "options"    -> label("optionsString", optional(text()))
  )(FormInput.apply)


  get("/") {
    contentType = "text/html"
    ssp("/form", "title" -> title, "formInput" -> lastInput, "request" -> request)
  }

  post("/") {
    contentType = "application/json"
    val attributes: List[(String, Any)] = List("title" -> title, "request" -> request)
    validate(input_form)(
      errors => {
        logger.info("Form validation failed..")
        logger.info(errors.toString())
        BadRequest(ssp("/form", attributes.:+("errors" -> convertErrors(errors)).:+("formInput" -> lastInput ):_*))
      },
      success = form => {
        lastInput = form
        logger.info(form.opts.toString())
        val strict: Boolean = form.opts.getOrElse("strict", "true") == "true"
        logger.info(strict.toString)
        val r : Future[Set[String]]= form.cmd match {
          case Command.getSubClasses => {
            val cls = Class(form.data)
            dbmodel.map(_.querySubClasses(cls, strict, true, true).map(_.getIRI.toString).toSet)
          }
          case Command.getSuperClasses => {
            val cls = Class(form.data)
            dbmodel.map(_.querySuperClasses(cls, strict, true, true).map(_.getIRI.toString).toSet)
          }
          case Command.getLabelForClass => {
            val cls = Class(form.data)
            dbmodel.map(_.getLabels(cls).toSet)
          }
          case Command.getLabelForObjectProperty => {
            val objProp = ObjectProperty(form.data)
            dbmodel.map(_.getLabels(objProp).toSet)
          }
          case _ => Future(Set("This command is not yet supported. Available commands are: " + Command.values.mkString("\n",",\n ", "\n")))
        }
        toJson(Await.result(r, 60.seconds))
      }
    )
  }

  def toJson(s: Set[String]): String = {
    new Gson().toJson(s.asJava)
  }
  def convertErrors(errors: Seq[(String, String)]):Map[String, String] = Map(errors :_*)
}

import de.tu_dresden.elk_web.web.ELKWebApp
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new ELKWebApp, "/*")
    //context.initParameters("org.scalatra.environment") = "production"
  }
}

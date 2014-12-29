import java.io.{PrintWriter, File}
import scala.xml._


case class View(path: Seq[Int], label: String, id: String)
case class ViewHolder(className: String)

trait Spec {
  val holderPrefix: String
  val layoutDir: File
  val srcDir: File
  val packageName: String
}
object GenerateViewHolder extends (Spec => Unit) {

  def GEN_START(s: String) = s"""    /* GENERATED CODE START $s */"""
  def GEN_END(s: String) = s"""    /* GENERATED CODE END $s */"""


  def singleXml(spec: Spec, file: File) = {
    val xml = XML.loadFile(file)

    if (xml.attributes.asAttrMap.get("holder").isDefined) {
      val classPath = spec.holderPrefix + xml.attributes.asAttrMap("holder")
      val xmlName = file.getName.replace(".xml", "")

      val data =  extractInfo(Seq.empty, "",xml)


      def __find_single_view(v: View) = {
        val __casts = v.path.map(_ => "((ViewGroup)").mkString
        s"${v.id} = ((${v.label}) ${__casts} view)" + v.path.map(i => s".getChildAt($i))").mkString + ";"
      }

      def __dec_single_view(v: View) = {
        s"${v.label} ${v.id};"
      }

      val GenStart = GEN_START(xmlName)
      val GenEnd = GEN_END(xmlName)

      val __content =
        s"""
         |${GenStart}
         |
         |    ${data.map(__dec_single_view).mkString("\n    ")}
         |
         |    private void __find_views_$xmlName(View view) {
         |        ${data.map(__find_single_view).mkString("\n        ")}
         |    }
         |
         |    protected View __inflate_view_$xmlName(LayoutInflater inflater, ViewGroup parent) {
         |        View view = inflater.inflate(${spec.packageName}.R.layout.${xmlName}, parent, false);
         |        __find_views_$xmlName(view);
         |        return view;
         |    }
         |
         |${GenEnd}
       """.stripMargin


      val className = classPath.split('.').last
      val javaFile = new File(spec.srcDir, classPath.replace('.', '/') + ".java")

      val sb = new StringBuilder()
      var skipping = false
      for (line <- io.Source.fromFile(javaFile).getLines()) {
        if (line.contains("class " + className)) {
          sb.append(line)
          sb.append("\n")
          sb.append(__content)
        } else if (line.contains(GenStart)) {
          skipping = true
        } else if (line.contains(GenEnd)) {
          skipping = false
        } else if (!skipping) {
          sb.append(line)
          sb.append("\n")
        }
      }
      writeTo(javaFile, sb.toString)
    }
  }


  def writeTo(des: File, content: String) = {
    des.mkdirs()
    des.delete()
    des.createNewFile()
    val writer = new PrintWriter(des)
    writer.write(content)
    writer.close()
  }

  def extractInfo(path: Seq[Int], parentId: String, r: Elem): Seq[View] = {
    val id_ = r.attributes.asAttrMap.get("android:id").map(_.split("/")(1))
    def childs(pl: String) = r.child.filter(_.isInstanceOf[Elem]).zipWithIndex.flatMap(a =>
      extractInfo(path :+ a._2, pl, a._1.asInstanceOf[Elem]))
    id_ match {
      case None => childs(parentId)
      case Some(id) =>
        val l = parentId + "_" + id
        View(path, r.label, l) +: childs(l)
    }
  }


  override def apply(v1: Spec): Unit = {
    v1.layoutDir.listFiles().filter(_.getName.endsWith(".xml")).map(a => singleXml(v1, a))
  }
}

object TantanSpec extends Spec {

  override val holderPrefix: String = "com.p1.mobile.putong.ui."
  override val layoutDir: File = new File("../../app/src/main/res/layout")
  override val srcDir: File = new File("../../app/src/main/java")
  override val packageName: String = "com.p1.mobile.putong"
}


GenerateViewHolder(TantanSpec)
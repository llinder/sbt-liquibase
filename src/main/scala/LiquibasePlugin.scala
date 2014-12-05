
package com.github.bigtoast.sbtliquibase

import java.io.PrintStream
import java.text.SimpleDateFormat

import liquibase.Liquibase
import liquibase.database.Database
import liquibase.diff.output.DiffOutputControl
import liquibase.integration.commandline.CommandLineUtils
import liquibase.resource.FileSystemResourceAccessor
import sbt.Keys._
import sbt._
import sbt.classpath._

object LiquibasePlugin extends AutoPlugin {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  val autoImport = Import

  override def projectSettings: Seq[Setting[_]] = liquibaseBaseSettings(Runtime) ++ inConfig(Test)(liquibaseBaseSettings(Test))

//  lazy val liquibaseSettings :Seq[Setting[_]] = Seq[Setting[_]](
  def liquibaseBaseSettings(conf: Configuration): Seq[Setting[_]] = {
    import com.github.bigtoast.sbtliquibase.LiquibasePlugin.autoImport._

    Seq[Setting[_]](
      liquibaseDefaultCatalog := None,
      liquibaseDefaultSchemaName := None,
      liquibaseChangelogCatalog := None,
      liquibaseChangelogSchemaName := None,
      liquibaseChangelog := "src/main/migrations/changelog.xml",
      liquibaseContext := "",
      //changelog <<= baseDirectory( _ / "src" / "main" / "migrations" /  "changelog.xml" absolutePath ),


      liquibaseDatabase <<= (
        liquibaseUrl,
        liquibaseUsername,
        liquibasePassword,
        liquibaseDriver,
        liquibaseDefaultCatalog,
        liquibaseDefaultSchemaName,
        liquibaseChangelogCatalog,
        liquibaseChangelogSchemaName,
        fullClasspath in conf
        ) map { (
                  url,
                  uname,
                  pass,
                  driver,
                  liquibaseDefaultCatalog,
                  liquibaseDefaultSchemaName,
                  liquibaseChangelogCatalog,
                  liquibaseChangelogSchemaName,
                  cpath) =>
        CommandLineUtils.createDatabaseObject(
          ClasspathUtilities.toLoader(cpath.map(_.data)),
          url,
          uname,
          pass,
          driver,
          liquibaseDefaultCatalog.getOrElse(null),
          liquibaseDefaultSchemaName.getOrElse(null),
          false, // outputDefaultCatalog
          true, // outputDefaultSchema
          null, // databaseClass
          null, // driverPropertiesFile
          null, // propertyProviderClass
          liquibaseChangelogCatalog.getOrElse(null),
          liquibaseChangelogSchemaName.getOrElse(null)
        )
      },

      liquibase <<= (liquibaseChangelog, liquibaseDatabase) map {
        (cLog: String, dBase: Database) =>
          new Liquibase(cLog, new FileSystemResourceAccessor, dBase)
      },

      liquibaseUpdate <<= (liquibase, liquibaseContext) map {
        (liquibase: Liquibase, context: String) =>
          liquibase.update(context)
      },

      liquibaseStatus <<= liquibase map {
        _.reportStatus(true, null.asInstanceOf[String], new LoggerWriter(ConsoleLogger()))
      },
      liquibaseClearChecksums <<= liquibase map {
        _.clearCheckSums()
      },
      liquibaseListLocks <<= (streams, liquibase) map { (out, lbase) => lbase.reportLocks(new PrintStream(out.binary()))},
      liquibaseReleaseLocks <<= (streams, liquibase) map { (out, lbase) => lbase.forceReleaseLocks()},
      liquibaseValidateChangelog <<= (streams, liquibase) map { (out, lbase) => lbase.validate()},
      liquibaseDbDoc <<= (streams, liquibase, target) map { (out, lbase, tdir) =>
        lbase.generateDocumentation(tdir / "liquibase-doc" absolutePath)
        out.log("Documentation generated in %s".format(tdir / "liquibase-doc" absolutePath))
      },

      liquibaseRollback <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.rollback(args.head, null.asInstanceOf[String])
          out.log("Rolled back to tag %s".format(args.head))
        }
      },

      liquibaseRollbackCount <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.rollback(args.head.toInt, null)
          out.log("Rolled back to count %s".format(args.head))
        }
      },

      liquibaseRollbackSql <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.rollback(args.head, null.asInstanceOf[String], out.text())
        }
      },

      liquibaseRollbackCountSql <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.rollback(args.head.toInt, null.asInstanceOf[String], out.text())
        }
      },

      liquibaseRollbackToDate <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.rollback(dateFormat.parse(args.mkString(" ")), null)
        }
      },

      liquibaseRollbackToDateSql <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.rollback(dateFormat.parse(args.mkString(" ")), null, out.text())
        }
      },

      liquibaseFutureRollbackSql <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.futureRollbackSQL(null, out.text())
        }
      },

      liquibaseTag <<= inputTask { (argTask) =>
        (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
          lbase.tag(args.head)
          out.log("Tagged db with %s for future rollback if needed".format(args.head))
        }
      },

      liquibaseGenerateChangelog <<= (
        streams,
        liquibase,
        liquibaseChangelog,
        liquibaseDefaultCatalog,
        liquibaseDefaultSchemaName,
        liquibaseChangelogCatalog,
        liquibaseChangelogSchemaName,
        baseDirectory) map { (
                               out,
                               lbase,
                               clog,
                               defaultCatalog,
                               defaultSchemaName,
                               liquibaseChangelogCatalog,
                               liquibaseChangelogSchemaName,
                               bdir) =>
        CommandLineUtils.doGenerateChangeLog(
          clog,
          lbase.getDatabase(),
          defaultCatalog.getOrElse(null),
          defaultSchemaName.getOrElse(null),
          null, // snapshotTypes
          null, // author
          null, // context
          bdir / "src" / "main" / "migrations" absolutePath,
          new DiffOutputControl())
      },

      liquibaseChangelogSyncSql <<= (streams, liquibase) map { (out, lbase) =>
        lbase.changeLogSync(null, out.text())
      },

      liquibaseDropAll <<= liquibase map {
        _.dropAll()
      }
    )
  }


}

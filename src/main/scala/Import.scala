package com.github.bigtoast.sbtliquibase

import liquibase.Liquibase
import liquibase.database.Database
import sbt.{SettingKey, InputKey, TaskKey}

object Import {
  val liquibaseUpdate = TaskKey[Unit]("liquibase-update", "Run a liquibase migration")
  val liquibaseStatus = TaskKey[Unit]("liquibase-status", "Print count of unrun change sets")
  val liquibaseClearChecksums = TaskKey[Unit]("liquibase-clear-checksums", "Removes all saved checksums from database log. Useful for 'MD5Sum Check Failed' errors")
  val liquibaseListLocks = TaskKey[Unit]("liquibase-list-locks", "Lists who currently has locks on the database changelog")
  val liquibaseReleaseLocks = TaskKey[Unit]("liquibase-release-locks", "Releases all locks on the database changelog")
  val liquibaseValidateChangelog = TaskKey[Unit]("liquibase-validate-changelog", "Checks changelog for errors")
  val liquibaseTag = InputKey[Unit]("liquibase-tag", "Tags the current database state for future rollback")
  val liquibaseDbDiff = TaskKey[Unit]("liquibase-db-diff", "( this isn't implemented yet ) Generate changeSet(s) to make Test DB match Development")
  val liquibaseDbDoc = TaskKey[Unit]("liquibase-db-doc", "Generates Javadoc-like documentation based on current database and change log")
  val liquibaseGenerateChangelog = TaskKey[Unit]("liquibase-generate-changelog", "Writes Change Log XML to copy the current state of the database to standard out")
  val liquibaseChangelogSyncSql = TaskKey[Unit]("liquibase-changelog-sync-sql", "Writes SQL to mark all changes as executed in the database to STDOUT")
  val liquibaseDropAll = TaskKey[Unit]("liquibase-drop-all", "Drop all database objects owned by user")

  val liquibaseRollback = InputKey[Unit]("liquibase-rollback", "<tag> Rolls back the database to the the state is was when the tag was applied")
  val liquibaseRollbackSql = InputKey[Unit]("liquibase-rollback-sql", "<tag> Writes SQL to roll back the database to that state it was in when the tag was applied to STDOUT")
  val liquibaseRollbackCount = InputKey[Unit]("liquibase-rollback-count", "<num>Rolls back the last <num> change sets applied to the database")
  val liquibaseRollbackCountSql = InputKey[Unit]("liquibase-rollback-count-sql", "<num> Writes SQL to roll back the last <num> change sets to STDOUT applied to the database")
  val liquibaseRollbackToDate = InputKey[Unit]("liquibase-rollback-to-date", "<date> Rolls back the database to the the state is was at the given date/time. Date Format: yyyy-MM-dd HH:mm:ss")
  val liquibaseRollbackToDateSql = InputKey[Unit]("liquibase-rollback-to-date-sql", "<date> Writes SQL to roll back the database to that state it was in at the given date/time version to STDOUT")
  val liquibaseFutureRollbackSql = InputKey[Unit]("liquibase-future-rollback-sql", " Writes SQL to roll back the database to the current state after the changes in the changelog have been applied")

  val liquibaseChangelog = SettingKey[String]("liquibase-changelog", "This is your liquibase changelog file to run.")
  val liquibaseUrl = SettingKey[String]("liquibase-url", "The url for liquibase")
  val liquibaseUsername = SettingKey[String]("liquibase-username", "username yo.")
  val liquibasePassword = SettingKey[String]("liquibase-password", "password")
  val liquibaseDriver = SettingKey[String]("liquibase-driver", "driver")
  val liquibaseDefaultCatalog = SettingKey[Option[String]]("liquibase-default-catalog", "default catalog")
  val liquibaseDefaultSchemaName = SettingKey[Option[String]]("liquibase-default-schema-name", "default schema name")
  val liquibaseChangelogCatalog = SettingKey[Option[String]]("liquibase-changelog-catalog", "changelog catalog")
  val liquibaseChangelogSchemaName = SettingKey[Option[String]]("liquibase-changelog-schema-name", "changelog schema name")
  val liquibaseContext = SettingKey[String]("liquibase-context", "changeSet contexts to execute")

  lazy val liquibaseDatabase = TaskKey[Database]("liquibase-database", "the database")
  lazy val liquibase = TaskKey[Liquibase]("liquibase", "liquibase object")
}

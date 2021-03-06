package com.hackethon.spark.file.parser.driver

import com.hackethon.spark.file.parser.core.NestedFileParserFactory
import com.hackethon.spark.file.parser.constants.FlattenStrategy
import com.hackethon.spark.file.parser.session.SparkSessionHandler
/**
 * @author Sai Krishna P
 */
object NestedDataParserStreamDriver extends App {
  if(args.length < 4){
		println("Not engough Arguments!")
		System.exit(1)
	}

	val fileType = args(0)
	val filePath = args(1)
	val outputPath = args(2)
	val flattenType = args(3)
	val sampleData = args(4)
	println("fileType :"+fileType)
	println("filePath :"+filePath)
	println("outputPath :"+outputPath)
	val spark = SparkSessionHandler.getSparkStreamSession()
	val schema = spark.read.json(sampleData).schema
	try{
		val parser = NestedFileParserFactory.getParser(fileType)
		val df = parser.readFileStream(filePath,spark,schema)
		val dfParsed = if(flattenType.equals("1")){parser.flatten(df, FlattenStrategy.SCHEMA_ITERATIVE)}else if(flattenType.equals("2")){parser.flatten(df, FlattenStrategy.SCHEMA_RECURSIVE)}else{parser.flatten(df, FlattenStrategy.SCHEMA_ITERATIVE)}
	
		val qry = parser.writeStream(dfParsed, outputPath)
		qry.awaitTermination()
	}catch{
		case e:Exception=> println("Exception message:"+e.getMessage)
											e.printStackTrace()
	}finally{
		spark.stop()
	}
}
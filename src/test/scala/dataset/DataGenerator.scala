package dataset

import java.io.{File, PrintWriter}

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source
import scala.util.Random

/**
  * Generates test input data for compression benchmarking.
  */
class DataGenerator extends FlatSpec with Matchers {
    "DataGenerator" should "generate benchmark data" in {
        // seeded random number generator
        val rand = new Random(4444)

        // lengths of files to generate
        val lengths: Seq[Int] = Seq(10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000)

        // output directory
        val outputDir = "benchmark/benchmark-data/"

        // entire text of Fox in Socks
        val foxInSocksSource = Source.fromFile("data/all-mtg-cards.txt")
        val foxInSocks = try foxInSocksSource.mkString finally foxInSocksSource.close()

        /**
          * Generates files of random, real and repeating data
          */
        def generateData(): Unit = {
            for (length <- lengths) {

                // make a file of random data
                val randomWriter = new PrintWriter(new File(outputDir + "random-" + length + ".txt"))

                // make a file from Fox in Socks
                val realWriter = new PrintWriter(new File(outputDir + "real-" + length + ".txt"))

                // make a file of all a's
                val repeatingWriter = new PrintWriter(new File(outputDir + "repeating-" + length + ".txt"))

                // write the data
                for (i <- 0 until length) {
                    randomWriter.write(rand.nextInt(256))
                    realWriter.write(foxInSocks(i % foxInSocks.length))
                    repeatingWriter.write("a")
                }

                // close the files
                randomWriter.close()
                realWriter.close()
                repeatingWriter.close()
            }
        }

        // actually do it
        generateData()
    }

    "DataGenerator" should "convert files into a chisel-readable format" in {
        // list of all the files to convert
        val dir = new File("benchmark/benchmark-data/")
        val files = dir.listFiles().filter(!_.getName.contains("_")).filter(!_.getName.contains("gitkeep"))

        /**
          * Converts a file into the right format
          */
        def convertFile(filename: String): Unit = {
            // read the original file
            val fileSource = Source.fromFile(filename)
            val fileText = try fileSource.mkString finally fileSource.close()

            // make a list of print writers
            var files: List[PrintWriter] = List()
            for (i <- 0 until 8)
                files = files :+ new PrintWriter(new File(filename.split(".txt")(0) + "_" + (7-i) + ".txt"))

            // copy the data
            for (i <- 0 until (Math.ceil(fileText.length / 8.0) * 8.0).toInt) {
                if (i < fileText.length)
                    files(i % 8).write("%02X".format(fileText(i).toByte) + "\n")
                else
                    files(i % 8).write("00")
            }

            // close the files
            for (writer <- files)
                writer.close()
        }

        // actually do it
        for (name <- files.map(_.toString)) {
            convertFile(name)
        }
    }
}

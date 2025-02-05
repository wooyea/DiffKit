/**
 * Copyright 2010-2011 Joseph Panico
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.diffkit.util.tst

import org.junit.Test

import java.io.ByteArrayInputStream;

import org.diffkit.util.DKStreamUtil;




/**
 * @author jpanico
 */
public class TestStreamUtil {

   @Test
   public void testCopy(){
      def inString = 
            """Beware the Jabberwock, my son! 
      	The jaws that bite, the claws that catch! 
         Beware the Jubjub bird, and shun
         The frumious Bandersnatch!"""
      println "inString->$inString"
      def inputStream = new ByteArrayInputStream(inString.getBytes())
      def outputStream = new ByteArrayOutputStream()
      DKStreamUtil.copy( inputStream, outputStream)
      def outString = outputStream.toString()
      
      assert inString == outString
   }
}

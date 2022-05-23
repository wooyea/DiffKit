package org.diffkit.util.tst

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

import org.diffkit.util.DKTimeUtil;
import groovy.time.TimeCategory
import org.junit.Test

/**
 * @author jpanico
 */
public class TestTimeUtil {

   @Test
   public void testCreateTime() {
      def time = DKTimeUtil.createTime( 5, 3, 20, 100)
      assert time
      assert time.getTime() == 18200100
   }

   @Test
   public static String localTime(long time) {
      use(TimeCategory) {
         return ((new Date(time))).format('yyyy-MM-dd HH:mm:ss')
      }
   }

   @Test
   public static String localDate(long time) {
      use(TimeCategory) {
         return ((new Date(time))).format('yyyy-MM-dd')
      }
   }
}

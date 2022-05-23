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
package org.diffkit.util.tst;

import org.diffkit.util.DKArrayUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jpanico
 */
public class JTestArrayUtil {

   @Test
   public void testRemoveMe() {
      char var = 'A' + 1;
      System.out.println("var->" + var);
      System.out.println("A+B->" + ("" + 'A' + 'B' + '\0' + 'C'));
   }

   @Test
   public void testIndicesOfIntersection() {
      String[] source = new String[] { "beware", "the", "jabberwocky" };
      String[] target = null;
      assertNull(DKArrayUtil.getIndicesOfIntersection(source, target));
      assertNull(DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[0];
      assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(source, target));
      assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "ack", "awk", "hack" };
      assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(source, target));
      assertEquals(null,
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "beware", "the", "jabberwocky" };
      assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));
      assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "jabberwocky", "the", "beware" };
      assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));
      assertEquals(new int[] { 0, 1, 2 },
         DKArrayUtil.getIndicesOfIntersection(target, source));

      target = new String[] { "jabberwocky" };
      assertEquals(new int[] { 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));

      target = new String[] { "jabberwocky", "ack" };
      assertEquals(new int[] { 2 },
         DKArrayUtil.getIndicesOfIntersection(source, target));

   }

   public void testRetain() {
      String[] target = new String[] { "beware", "the", "jabberwocky" };

      assertNull(DKArrayUtil.retainElementsAtIndices(null, null));
      assertNull(DKArrayUtil.retainElementsAtIndices(target, null));
      assertNull(DKArrayUtil.retainElementsAtIndices(null, new int[] { 2 }));
      assertNull(DKArrayUtil.retainElementsAtIndices(null, new int[0]));
      assertNull(DKArrayUtil.retainElementsAtIndices(target, new int[0]));
      assertArrayEquals(target,
         DKArrayUtil.retainElementsAtIndices(target, new int[] { 0, 1, 2 }));
      assertArrayEquals(new String[] { "the" },
         DKArrayUtil.retainElementsAtIndices(target, new int[] { 1 }));

   }

   public void testIntersection() {
      String[] source = new String[] { "beware", "the", "jabberwocky" };
      String[] target = null;
      assertNull(DKArrayUtil.getIntersection(source, target));
      assertNull(DKArrayUtil.getIntersection(target, source));

      target = new String[0];
      assertArrayEquals(new String[0],
         DKArrayUtil.getIntersection(source, target));
      assertArrayEquals(new String[0],
         DKArrayUtil.getIntersection(target, source));

      target = new String[] { "ack", "awk", "hack" };
      assertArrayEquals(new String[0],
         DKArrayUtil.getIntersection(source, target));
      assertArrayEquals(new String[0],
         DKArrayUtil.getIntersection(target, source));

      target = new String[] { "beware", "the", "jabberwocky" };
      assertArrayEquals(source, DKArrayUtil.getIntersection(source, target));
      assertArrayEquals(source, DKArrayUtil.getIntersection(target, source));

      target = new String[] { "jabberwocky", "the", "beware" };
      assertArrayEquals(source, DKArrayUtil.getIntersection(source, target));
      assertArrayEquals(target, DKArrayUtil.getIntersection(target, source));

      target = new String[] { "jabberwocky" };
      assertArrayEquals(target, DKArrayUtil.getIntersection(source, target));

      target = new String[] { "jabberwocky", "ack" };
      assertArrayEquals(new String[] { "jabberwocky" },
         DKArrayUtil.getIntersection(source, target));

   }

   public void testSubarray() {
      String[] array = new String[] { "beware", "the", "jabberwocky" };
      assertArrayEquals(array, DKArrayUtil.subarray(array, 0, array.length));
      assertArrayEquals(new String[] { "beware", "the" },
         DKArrayUtil.subarray(array, 0, 2));
      assertArrayEquals(new String[] { "the", "jabberwocky" },
         DKArrayUtil.subarray(array, 1, 3));
   }

   public void testCreateArray() {
      String[] array = DKArrayUtil.createArray(String.class, 5);
      assertNotNull(array);
      try {
         String[] oArray = (String[]) DKArrayUtil.createArray(Object.class, 5);
         fail("Should raise an ClassCastException");
      }
      catch (ClassCastException e) {
      }
   }
}

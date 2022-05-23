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
package org.diffkit.diff.sns.tst

import org.apache.commons.lang3.ClassUtils
import org.diffkit.diff.engine.DKColumnModel
import org.diffkit.diff.engine.DKTableModel
import org.diffkit.diff.sns.DKFileSource
import org.diffkit.util.DKResourceUtil
import org.diffkit.util.DKStringUtil
import org.junit.Test

/**
 * @author jpanico
 */
public class TestFileSource {

    @Test
    public void testDefaultModelWithKeyColumnNames() {
        String sourceFileName = 'lhs1.csv'
        String sourceFilePath = ClassUtils.getPackageName(this.getClass())
        sourceFilePath = DKStringUtil.packageNameToResourcePath(sourceFilePath) + sourceFileName
        def sourceFile = DKResourceUtil.findResourceAsFile(sourceFilePath)
        println "sourceFile->$sourceFile"
        DKFileSource source = new DKFileSource(sourceFile.absolutePath, null, (String[]) ['column2'], null, '\\,', true, true)
        def model = source.model
        assert model
        def columns = model.columns
        assert columns
        assert columns.length == 3
        assert columns[0].name == 'column1'
        assert model.key == [1]
    }

    @Test
    public void testDefaultModel() {
        String sourceFileName = 'lhs1.csv'
        String sourceFilePath = ClassUtils.getPackageName(this.getClass())
        sourceFilePath = DKStringUtil.packageNameToResourcePath(sourceFilePath) + sourceFileName
        def sourceFile = DKResourceUtil.findResourceAsFile(sourceFilePath)
        println "sourceFile->$sourceFile"
        DKFileSource source = new DKFileSource(sourceFile.absolutePath, null, null, null, '\\,', true, true)
        def model = source.model
        assert model
        def columns = model.columns
        assert columns
        assert columns.length == 3
        assert columns[0].name == 'column1'
        assert model.key == [0]
    }

    @Test
    public void testRead() {
        String sourceFileName = 'lhs1.csv'
        String sourceFilePath = ClassUtils.getPackageName(this.getClass())
        sourceFilePath = DKStringUtil.packageNameToResourcePath(sourceFilePath) + sourceFileName
        def sourceFile = DKResourceUtil.findResourceAsFile(sourceFilePath)
        println "sourceFile->$sourceFile"

        DKTableModel model = this.createSimpleTableModel()
        DKFileSource source = new DKFileSource(sourceFile.absolutePath, model, null, null, '\\,', true, true)
        println "source->$source"

        source.open(null)
        assert source.getNextRow() == (Object[]) ['1111', '1111', 1]
        assert source.getNextRow() == (Object[]) ['1111', '1111', 2]
        assert source.getNextRow() == (Object[]) ['4444', '4444', 1]
        assert source.getNextRow() == (Object[]) ['4444', '4444', 2]
        assert source.getNextRow() == (Object[]) ['6666', '6666', null]
        assert source.getNextRow() == (Object[]) ['6666', '6666', 2]
        assert source.getNextRow() == (Object[]) ['7777', '7777,7777', 3]
        assert !source.getNextRow()
        assert !source.getNextRow()
        source.close(null)
    }

    @Test
    private DKTableModel createSimpleTableModel() {
        DKColumnModel column1 = [0, 'column1', DKColumnModel.Type.STRING]
        DKColumnModel column2 = [1, 'column2', DKColumnModel.Type.STRING]
        DKColumnModel column3 = [2, 'column3', DKColumnModel.Type.INTEGER, '###']
        DKColumnModel[] columns = [column1, column2, column3]
        int[] key = [0, 2]

        return new DKTableModel("simple_table_model", columns, key)
    }

    @Test
    public void testReadWithQuotedFields() {
        String sourceFileName = 'customers.csv'
        String sourceFilePath = ClassUtils.getPackageName(this.getClass())
        sourceFilePath = DKStringUtil.packageNameToResourcePath(sourceFilePath) + sourceFileName
        def sourceFile = DKResourceUtil.findResourceAsFile(sourceFilePath)
        println "sourceFile->$sourceFile"

        DKTableModel model = this.createCustomerTableModel()
        DKFileSource source = new DKFileSource(sourceFile.absolutePath, model, null, null, '\\,', true, true)
        println "source->$source"

        source.open(null)
        assert source.getNextRow() == (Object[]) ['steve', 'jobs', 'infinite, loop', 'Cupertino', 'usa', 54]
        assert source.getNextRow() == (Object[]) ['rob', 'smith', '100 spruce st', 'Phila', 'usa', 50]
        assert !source.getNextRow()
        source.close(null)
    }

    private DKTableModel createCustomerTableModel() {
        DKColumnModel first_name = [0, 'first_name', DKColumnModel.Type.STRING]
        DKColumnModel last_name = [1, 'last_name', DKColumnModel.Type.STRING]
        DKColumnModel address = [2, 'address', DKColumnModel.Type.STRING]
        DKColumnModel city = [3, 'city', DKColumnModel.Type.STRING]
        DKColumnModel country = [4, 'country', DKColumnModel.Type.STRING]
        DKColumnModel age = [5, 'age', DKColumnModel.Type.INTEGER, '##']
        DKColumnModel[] columns = [first_name, last_name, address, city, country, age]
        int[] key = [0, 1]

        return new DKTableModel("simple_table_model", columns, key)
    }
}

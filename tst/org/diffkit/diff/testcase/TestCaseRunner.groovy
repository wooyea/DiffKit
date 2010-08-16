package org.diffkit.diff.testcase

/**
 * Copyright 2010 Joseph Panico
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



import java.net.URL;
import java.util.regex.Pattern 

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext 
import org.springframework.context.support.AbstractXmlApplicationContext 
import org.springframework.context.support.ClassPathXmlApplicationContext 
import org.springframework.context.support.FileSystemXmlApplicationContext 

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.ClassUtils 

import org.diffkit.common.DKRegexFilenameFilter;
import org.diffkit.common.DKValidate;

import org.diffkit.db.DKDBConnectionSource 
import org.diffkit.db.DKDBH2Loader 
import org.diffkit.db.DKDBTableLoader 
import org.diffkit.db.tst.DBTestSetup 
import org.diffkit.diff.conf.DKPlan 
import org.diffkit.diff.engine.DKSink 
import org.diffkit.diff.engine.DKSource 
import org.diffkit.diff.engine.DKSourceSink 
import org.diffkit.diff.engine.DKSourceSink.Kind
import org.diffkit.diff.sns.DKFileSink 
import org.diffkit.diff.sns.DKFileSource 
import org.diffkit.diff.sns.DKWriterSink 
import org.diffkit.util.DKResourceUtil;
import org.diffkit.util.DKStringUtil 


/**
 * @author jpanico
 */
public class TestCaseRunner implements Runnable {
   
   public static final String TEST_CASE_FILE_NAME = 'testcaserunner.xml'
   public static final String TEST_CASE_PLAN_FILE_PATTERN = 'test(\\d*)\\.plan\\.xml'
   public static final DKRegexFilenameFilter TEST_CASE_PLAN_FILTER = new DKRegexFilenameFilter(TEST_CASE_PLAN_FILE_PATTERN);
   private static final List TEST_CASE_DATA_SUFFIXES = ['xml', 'diff', 'csv', 'txt']
   private static final FileFilter TEST_CASE_DATA_FILTER = new SuffixFileFilter(TEST_CASE_DATA_SUFFIXES)
   
   private List<Integer> _testCaseNumbers
   private String _dataPath
   private List<TestCase> _allTestCases
   private final Logger _log = LoggerFactory.getLogger(this.getClass())
   
   
   /**
    * @param dataPath_ expressed as a classpath resource
    */
   public TestCaseRunner(List<Integer> testCaseNumbers_){
      _log.debug("testCaseNumbers_->{}",testCaseNumbers_)
      _testCaseNumbers = testCaseNumbers_
      _dataPath =  getDefaultDataPath()
      DKValidate.notNull(_dataPath)
   }
   
   public void run(){
      def runnerRun = this.setupRunnerRun()
      _allTestCases = this.fetchAllTestCases(runnerRun)
      _log.debug("_allTestCases->{}",_allTestCases)
      def List<TestCase> testCases = null
      if( !_testCaseNumbers) {
         _log.info("no TestCase numbers specified; will use all TestCases found at _dataPath->$_dataPath")
         testCases = _allTestCases
      }
      else
         testCases = _allTestCases.findAll {
            _testCaseNumbers.contains(it.id)
         }
      
      Collections.sort(testCases)
      _log.info("testCases->{}",testCases)
      testCases.each {
         this.run(it, runnerRun)
      }
      this.report(runnerRun)
   }
   
   /**
    * copy the data files into the TestCaseRunnerRun working directory
    */
   private TestCaseRunnerRun setupRunnerRun(){
      TestCaseRunnerRun runnerRun = [new File('./')]
      def classLoader = this.class.classLoader
      _log.info("classLoader->{}",classLoader)
      DKResourceUtil.addResourceDir(runnerRun.dir)
      URL dataPathUrl = classLoader.getResource(_dataPath)
      _log.info("dataPathUrl->{}",dataPathUrl)
      if(dataPathUrl.toExternalForm().startsWith("jar:")){
      }
      else {
         File dataDir = [dataPathUrl.toURI()]
         FileUtils.copyDirectory( dataDir, runnerRun.dir, TEST_CASE_DATA_FILTER)
      }
      return runnerRun
   }
   
   private TestCaseRun run(TestCase testCase_, TestCaseRunnerRun runnerRun_){
      _log.info("testCase_->{}",testCase_.description)
      this.setupDB( testCase_)
      TestCaseRun run = [testCase_, this.getPlan(testCase_)]
      runnerRun_.addRun( run)
      _log.debug("run->{}",run)
      this.validate(run)
      this.setup(run, runnerRun_)
      this.execute(run)
      return run
   }
   
   private void setupDB(TestCase testCase_) {
      DBTestSetup.setupDB(testCase_.dbSetupFile, testCase_.lhsSourceFile, testCase_.rhsSourceFile)
   }
   
   private DKPlan getPlan(TestCase testCase_){
      ApplicationContext context = new FileSystemXmlApplicationContext('file:'+testCase_.planFile.absolutePath);
      assert context
      def plan = context.getBean('plan')
      _log.debug("plan->{}",plan)
      if(!plan)
         throw new RuntimeException("no 'plan' bean in planFile->${testCase_.planFile}")
      return plan
   }
   
   private void report(TestCaseRunnerRun runnerRun_){
      println "\nTestCaseRunnerRun -- ${runnerRun_.dir}\n=================="
      println "\n\tTestCaseRuns\n\t------------"
      runnerRun_.testCaseRuns.each { println "\t${it.report}" }
      println "\n"
   }
   
   private void execute(TestCaseRun run_){
      run_.execute()
   }
   
   private void setup(TestCaseRun run_, TestCaseRunnerRun runnerRun_){
      run_.plan.lhsSource = this.setupSource( run_.plan.lhsSource, runnerRun_)
      run_.plan.rhsSource = this.setupSource( run_.plan.rhsSource, runnerRun_)
      run_.plan.sink = this.setupSink( run_.plan.sink, runnerRun_)
   }
   
   private DKSource setupSource(DKSource source_, TestCaseRunnerRun runnerRun_){
//      if(source_.kind == DKSourceSink.Kind.FILE)
//         return this.setupFileSource( source_, runnerRun_)
      return source_
   }
   
   private DKSink setupSink(DKSink sink_, TestCaseRunnerRun runnerRun_){
      if(sink_.kind == DKSourceSink.Kind.FILE)
         return this.setupFileSink( sink_, runnerRun_)
      else
         throw new RuntimeException("unrecognized sink_.kind->${sink_.kind}")
   }
   
   private DKSource setupFileSource(DKFileSource source_, TestCaseRunnerRun runnerRun_){
      File newSourcePath = [runnerRun_.dir, source_.file.path]
      _log.debug("newSourcePath->{}",newSourcePath)
      return new DKFileSource(newSourcePath.absolutePath, source_.model, 
      source_.keyColumnNames, source_.readColumnIdxs, source_.delimeter, 
      source_.isSorted, source_.validateLazily )
   }
   
   private DKSink setupFileSink(DKWriterSink sink_, TestCaseRunnerRun runnerRun_){
      File newSinkPath = [runnerRun_.dir, sink_.file.path]
      _log.debug("newSinkPath->{}",newSinkPath)
      return new DKFileSink(newSinkPath.absolutePath)
   }
   
   private DKDBTableLoader getLoaderForSource(DKDBConnectionSource source_){
      return new DKDBH2Loader(source_)
   }
   
   /**
    * make sure that plan has proper characteristics for TestCases
    */
   private void validate(TestCaseRun run_){
      _log.debug("run_->{}",run_)
      def lhsSource = run_.plan.lhsSource
      if(!lhsSource)
         throw new RuntimeException(String.format("no lhsSoure for plan->%s", run_.plan))
      def rhsSource = run_.plan.rhsSource
      if(!rhsSource)
         throw new RuntimeException(String.format("no rhsSource for plan->%s", run_.plan))
      this.validateSource(run_.plan.lhsSource)
      this.validateSource(run_.plan.rhsSource)
      this.validateSink(run_.plan.sink)
   }
   
   /**
    * only work with File and DB sources; if file, ensure that data file matches that listed in TestCase
    */
   private void validateSource(DKSource source_){
      _log.debug("source_->{}",source_)
      Kind kind = source_.kind
      if(!((kind == DKSourceSink.Kind.FILE)||(kind == DKSourceSink.Kind.DB)))
         throw new RuntimeException("can only work with Sources of Kind->${[DKSourceSink.Kind.FILE,DKSourceSink.Kind.DB]}")
   }
   /**
    * only work with File and DB sources; if file, ensure that data file matches that listed in TestCase
    */
   private void validateSink(DKSink sink_){
      _log.debug("sink_->{}",sink_)
      Kind kind = sink_.kind
      if(!(kind == DKSourceSink.Kind.FILE))
         throw new RuntimeException("can only work with Sources of Kind->${[DKSourceSink.Kind.FILE]}")
      File sinkFile = sink_.file
      String sinkPath = sinkFile.path
      if(!sinkPath.startsWith('./'))
         throw new RuntimeException("sinkPath must be relative path starting with ./")
   }
   
   private List<TestCase> fetchAllTestCases(TestCaseRunnerRun runnerRun_){
      File[] planFiles = runnerRun_.dir.listFiles(TEST_CASE_PLAN_FILTER)
      if(!planFiles)
         return null
      def testCases = new ArrayList(planFiles.length)
      planFiles.each {
         def testCase = this.createTestCase(it, runnerRun_.dir)
         if(testCase)
            testCases.add(testCase)
      }
      return testCases
   }
   
   private TestCase createTestCase(File planFile_, File dir_){
      def matcher = Pattern.compile(TEST_CASE_PLAN_FILE_PATTERN).matcher(planFile_.name)
      matcher.matches()
      def numberString = matcher.group(1)
      _log.debug("numberString->{}",numberString)		
      def number = Integer.parseInt(numberString)
      def dbSetupFile = new File(dir_, "test${numberString}.dbsetup.xml")
      _log.debug("dbSetupFile->{}",dbSetupFile)
      if(!dbSetupFile.exists())
         dbSetupFile = null
      def name = "test$numberString"
      def lhsSourceFile = new File(dir_, "test${numberString}.lhs.csv")
      def rhsSourceFile = new File(dir_, "test${numberString}.rhs.csv")
      def expectedFile = new File(dir_, "test${numberString}.expected.diff")
      def testCase= new TestCase(number,name,null, dbSetupFile, lhsSourceFile, rhsSourceFile, planFile_, expectedFile)
      return testCase
   }
   
   private static String getDefaultDataPath(){
      return DKStringUtil.packageNameToResourcePath(ClassUtils.getPackageName(TestCaseRunner.class) )
   }
   
   public static void main(String[] args_){
      println "now->${new Date()}"
      println "class->${TestCaseRunner.class}"
      def testCasesResourcePath =  getDefaultDataPath() + TEST_CASE_FILE_NAME
      println "testCasesResourcePath->$testCasesResourcePath"
      AbstractXmlApplicationContext context = new ClassPathXmlApplicationContext((String[]) [ testCasesResourcePath ], false)
      context.setClassLoader(TestCaseRunner.class.getClassLoader())
      context.refresh()
      assert context
      
      def runner = context.getBean('runner')
      println "runner->$runner"
      assert runner
      runner.run()
   }
   
   private void setupTestCase(TestCase testCase_){
      _log.info("setting up testCase_->{}",testCase_)
   }
}


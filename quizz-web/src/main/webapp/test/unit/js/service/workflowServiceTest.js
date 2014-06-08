describe('WorkflowService test', function() {
  var QUIZ_ID = 'test_quiz';

  beforeEach(module('quizz'));

  it('test next question gold', inject(['workflowService',
    function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [{id: 3}];
      questionsMap['collection'] = [{id: 5}];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();
      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      expect(workflowService.getCurrentQuestion()['id']).toBe(3);
    }
  ]));

  it('test next question silver', inject(['workflowService',
    function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [{id: 3}];
      questionsMap['collection'] = [{id: 5}];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      workflowService.setNextQuestionGold(false);
      workflowService.getNewCurrentQuestion();
      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      expect(workflowService.getCurrentQuestion()['id']).toBe(5);
    }
  ]));

  it('test no gold question', inject(['workflowService',
    function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [];
      questionsMap['collection'] = [{id: 5}];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      // if no gold question, we will pick collection question.
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();
      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      expect(workflowService.getCurrentQuestion()['id']).toBe(5);
    }
  ]));

  it('test not enough gold question but has collection question',
    inject(['workflowService', function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [{id: 10}];
      questionsMap['collection'] = [{id: 5}];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      // Picks two gold questions.
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();
      workflowService.incCurrentQuestionIndex();
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();

      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      // but since we ran out of gold question, we will switch to collection
      // question instead.
      expect(workflowService.getCurrentQuestion()['id']).toBe(5);
    }
  ]));

  it('test not enough gold question and no collection question',
    inject(['workflowService', function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [{id: 10}, {id: 7}];
      questionsMap['collection'] = [];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      // Picks three gold questions.
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();
      workflowService.incCurrentQuestionIndex();
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();
      workflowService.incCurrentQuestionIndex();
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();

      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      // Since there is no collection question to fallback to, we have to
      // repeat asking gold question.
      expect(workflowService.getCurrentQuestion()['id']).toBe(10);
    }
  ]));

  it('test not enough collection question and has calibration question',
    inject(['workflowService', function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [{id: 10}];
      questionsMap['collection'] = [{id: 7}];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      // Picks two collection questions.
      workflowService.setNextQuestionGold(false);
      workflowService.getNewCurrentQuestion();
      workflowService.incCurrentQuestionIndex();
      workflowService.setNextQuestionGold(false);
      workflowService.getNewCurrentQuestion();

      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      // Always repeat collection questions and not gold question even if we
      // run out of collection questions.
      expect(workflowService.getCurrentQuestion()['id']).toBe(7);
    }
  ]));

  it('test second collection question',
    inject(['workflowService', function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [{id: 10}];
      questionsMap['collection'] = [{id: 7}, {id: 9}];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      // Picks two collection questions.
      workflowService.setNextQuestionGold(false);
      workflowService.getNewCurrentQuestion();
      workflowService.incCurrentQuestionIndex();
      workflowService.setNextQuestionGold(false);
      workflowService.getNewCurrentQuestion();

      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      expect(workflowService.getCurrentQuestion()['id']).toBe(9);
    }
  ]));

  it('test second calibration question',
    inject(['workflowService', function(workflowService) {
      var questionsMap = {};
      questionsMap['calibration'] = [{id: 10}, {id: 8}];
      questionsMap['collection'] = [{id: 7}, {id: 9}];

      expect(workflowService.getCurrentQuestion()).toBe(null);
      workflowService.setQuestions(questionsMap, QUIZ_ID);

      // Picks two gold questions.
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();
      workflowService.incCurrentQuestionIndex();
      workflowService.setNextQuestionGold(true);
      workflowService.getNewCurrentQuestion();

      expect(workflowService.getCurrentQuestion()).not.toBe(null);
      expect(workflowService.getCurrentQuestion()['id']).toBe(8);
    }
  ]));
});

describe('quizz e2e test', function() {

  var ptor = protractor.getInstance();
  browser.get('/');

  it('should load the list page', function() {
    expect(ptor.getCurrentUrl()).toMatch(/\/list/);
  });
  
  it('the table with quizzes should be exists', function() {
    var ele = by.className('table');
    expect(ptor.isElementPresent(ele)).toBe(true);
  });
  
  it('quiz with name testQuizName should be exists', function() {
    var quizzes = element.all(by.repeater('quiz in quizes'));
    expect(quizzes.count()).toNotBe(0);
  });
  
  it('should open a quiz', function() {
    openQuiz('testQuizName');
  });
  
  for(var idx=1; idx<=10; idx++){
    nextQuestion(idx);
  }
  
  it('should open a summary', function() {
    expect(ptor.getCurrentUrl()).toMatch(/\/summary/);
  });
  
  it('should show a correct summary', function() {
    var progress = element(by.tagName('p'));
    expect(progress.getText()).toBe('You have answered correctly 10 out of 10 questions.');
  });
  
  it('should open next question', function() {
    element(by.buttonText('Click here to start again')).click();
  });
  
  function nextQuestion(idx) {
    it('it should open questions', function() {
      expect(ptor.getCurrentUrl()).toMatch(/\/quiz/);
    });

    it('it should show correct progress', function() {
      var progress = element(by.css('.q-progress'));
      expect(progress.getText()).toBe('Question ' + idx + ' out of 10');
    });
    
    it('should show correct answers', function() {
      var correctAnswers = ptor.findElement(by.id('correctAnswers'));
      expect(correctAnswers.getText()).toEqual('Correct Answers: ' + (idx-1) + '/' + (idx-1));
    });
    
    it('should show correct percentage', function() {
      var correctAnswers = ptor.findElement(by.id('percentageCorrect'));
      expect(correctAnswers.getText()).toEqual('Correct (%): ' + (idx==1?0:100) + '%');
    });
    
    it('should select a first answer', function() {
      element.all(by.tagName('button')).first().click();
    });
    
    it('should open a feedback', function() {
      expect(ptor.getCurrentUrl()).toMatch(/\/feedback/);
    });
    
    it('should open next question', function() {
      element(by.buttonText('Next question')).click();
    });
  }
  
  function openQuiz(name) {
    var quizzes  = element.all(by.repeater('quiz in quizes'));
    expect(quizzes.count()).toNotBe(0);
    quizzes.map(function(quizElement, index) {
      return {
        index: index,
        text: quizElement.getText()
      };
    }).then(function(items) {
      var length = items.length;
      for (var i = 0; i < length; i++) {
        quizzes.get(items[i].index).findElement(by.tagName('a')).click();
      }
    });
  }

});
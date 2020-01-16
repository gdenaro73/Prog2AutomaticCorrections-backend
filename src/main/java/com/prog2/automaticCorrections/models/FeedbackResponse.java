package com.prog2.automaticCorrections.models;

/**
 * @author benedettoraviotta
 * model for checker response, used to give student feedback on moodle
 */

public class FeedbackResponse {
 
	
	private String checkerResponse;

	
	public String getCheckerResponse() {
		return checkerResponse;
	}
	public void setCheckerResponse(String checkerResponse) {
		this.checkerResponse = checkerResponse;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getCheckerResponse();
	}

}


package com.randomsymphony.games.ochre.ui;

import com.randomsymphony.games.ochre.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScoreBoard extends Fragment {

	private View mContent;
	private TextView mTeamOneName;
	private TextView mTeamOneScore;
	private TextView mTeamTwoName;
	private TextView mTeamTwoScore;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContent = inflater.inflate(R.layout.fragment_score_board, null);
		mTeamOneName = (TextView) mContent.findViewById(R.id.name_team_one);
		mTeamTwoName = (TextView) mContent.findViewById(R.id.name_team_two);
		mTeamOneScore = (TextView) mContent.findViewById(R.id.score_team_one);
		mTeamTwoScore = (TextView) mContent.findViewById(R.id.score_team_two);
		
		return mContent;
	}
	
	public void setTeamOneName(String name) {
		mTeamOneName.setText(name);
	}
	
	public void setTeamTwoName(String name) {
		mTeamTwoName.setText(name);
	}
	
	public void setTeamOneScore(int score) {
		mTeamOneScore.setText(String.valueOf(score));
	}
	
	public void setTeamTwoScore(int score) {
		mTeamTwoScore.setText(String.valueOf(score));
	}
}

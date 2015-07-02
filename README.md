# Ochre
An implementation of the classic card game Euchre for the Android platform. Intended to be a multi-player version where opponents may be on the internet or a collection of devices may play against each other locally. We'll see how far we get. :-)  

In the multi-device scenario a given device may subscribe to a game as one or more of the entities in the game. Conceptually every game has five entities, one entity for each of the players and a fifth entity for game state. The game state display includes the current trump, current cards in the trick, and score.  A very straight-forward use case for multiple devices is that you have four people that are in the same room. Each has a phone on which they are subscribed as one of the players. A tablet is on the table around which the players are sitting. The tablet is subscribed as the game state.  

Another common scenario would be for four different people all in different physical locations to each subscribe as a player entity and game board entity. In this case they would see their cards, the board, and some minimal representation of the other players.

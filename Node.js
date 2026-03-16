socket.on('player_move', (data) => {
  const player = game.players[socket.id];
  
  if (data.type === 'bet') {
    if (player.chips >= data.amount) {
      player.chips -= data.amount;
      game.pot += data.amount;
      
      // Tell everyone the pot changed
      io.emit('update_pot', { total: game.pot });
      // Move to the next player's turn
      nextTurn();
    } else {
      socket.emit('error', 'Insufficient chips!');
    }
  }
});

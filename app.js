let stompClient = null;
let connected = false;

function connectIfNeeded(callback) {
  if (connected && stompClient) {
    callback();
    return;
  }

  const socket = new SockJS('/poker');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function () {
    connected = true;

    stompClient.subscribe('/user/queue/state', function (message) {
      const state = JSON.parse(message.body);
      renderState(state);
    });

    callback();
  });
}

function joinGame() {
  const name = document.getElementById('nameInput').value.trim() || 'Player';

  connectIfNeeded(() => {
    stompClient.send('/app/join', {}, JSON.stringify({ name }));
  });
}

function startGame() {
  if (!connected || !stompClient) return;
  stompClient.send('/app/start', {}, JSON.stringify({}));
}

function sendAction(action) {
  if (!connected || !stompClient) return;
  stompClient.send('/app/action', {}, JSON.stringify({ action, amount: 0 }));
}

function raiseAction() {
  if (!connected || !stompClient) return;

  const amount = parseInt(document.getElementById('raiseAmount').value) || 0;
  stompClient.send('/app/action', {}, JSON.stringify({ action: 'RAISE', amount }));
}

function renderState(state) {
  document.getElementById('phase').innerText = state.phase;
  document.getElementById('pot').innerText = state.pot;
  document.getElementById('highestBet').innerText = state.highestBet;
  document.getElementById('message').innerText = state.message + (state.yourTurn ? ' | YOUR TURN' : '');

  const communityCards = document.getElementById('communityCards');
  communityCards.innerHTML = '';
  state.communityCards.forEach(card => {
    communityCards.appendChild(createCard(card));
  });

  const yourHand = document.getElementById('yourHand');
  yourHand.innerHTML = '';
  state.yourHand.forEach(card => {
    yourHand.appendChild(createCard(card));
  });

  const playersList = document.getElementById('playersList');
  playersList.innerHTML = '';

  state.players.forEach(player => {
    const div = document.createElement('div');
    div.className = 'player-card';

    if (player.id === state.currentTurnPlayerId) {
      div.classList.add('player-turn');
    }

    div.innerHTML = `
      <strong>${player.name}</strong><br>
      Chips: $${player.chips}<br>
      Current Bet: $${player.currentBet}<br>
      Status: ${player.folded ? 'Folded' : player.allIn ? 'All-In' : 'Active'}
      ${player.id === state.yourId ? '<br><span class="muted">(You)</span>' : ''}
    `;

    playersList.appendChild(div);
  });
}

function createCard(card) {
  const div = document.createElement('div');
  div.className = 'card';
  div.innerText = `${card.rank}\n${card.suit}`;
  return div;
}

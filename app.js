let stompClient = null;

function connect() {
  const socket = new SockJS('/poker');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function () {
    stompClient.subscribe('/topic/game', function (message) {
      const state = JSON.parse(message.body);
      renderGame(state);
    });
  });
}

function joinGame() {
  const name = document.getElementById('nameInput').value || 'Player';
  if (!stompClient) connect();

  setTimeout(() => {
    stompClient.send('/app/join', {}, JSON.stringify({ name }));
  }, 500);
}

function startGame() {
  if (stompClient) {
    stompClient.send('/app/start', {}, {});
  }
}

function sendAction(action) {
  if (stompClient) {
    stompClient.send('/app/action', {}, JSON.stringify({ action, amount: 0 }));
  }
}

function raiseBet() {
  const amount = parseInt(document.getElementById('raiseAmount').value) || 0;
  if (stompClient) {
    stompClient.send('/app/action', {}, JSON.stringify({ action: 'RAISE', amount }));
  }
}

function renderGame(state) {
  document.getElementById('phase').innerText = state.phase;
  document.getElementById('pot').innerText = state.pot;
  document.getElementById('message').innerText = state.message;

  const community = document.getElementById('communityCards');
  community.innerHTML = '';
  state.communityCards.forEach(card => {
    const div = document.createElement('div');
    div.className = 'card';
    div.innerText = `${card.rank}\n${card.suit}`;
    community.appendChild(div);
  });

  const players = document.getElementById('players');
  players.innerHTML = '';
  state.players.forEach((p, index) => {
    const div = document.createElement('div');
    div.className = 'player';
    div.innerHTML = `
      <strong>${p.name}</strong> ${index === state.currentPlayerIndex ? '⬅️ TURN' : ''}
      <br>Chips: $${p.chips}
      <br>Bet: $${p.currentBet}
      <br>Status: ${p.folded ? 'Folded' : 'Active'}
      <br>Hand: ${p.hand.map(c => c.rank + ' of ' + c.suit).join(', ')}
    `;
    players.appendChild(div);
  });
}

let stompClient;
let subscriptions = {};  // 구독을 관리할 객체
let isConnected = false; // WebSocket 연결 상태를 추적


// 페이지 로드 시 로컬 스토리지에서 구독한 도시를 불러옴
window.onload = () => {
    const savedCities = JSON.parse(localStorage.getItem("subscribedCities")) || [];
    if (savedCities.length > 0) {
        // WebSocket 연결 시도
        connectWebSocket(() => {
            savedCities.forEach(city => subscribeToCity(city));
        });
    }
};

function connectWebSocket(callback) {
    if (isConnected) return callback(); // 이미 연결된 경우 바로 콜백 호출

    const socket = new SockJS('/ws/weather');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        isConnected = true;
        console.log("WebSocket 연결 성공");

        // 연결 완료 후 콜백 실행
        if (callback) callback();
    });
}


async function getWeather() {
    const cityInput = document.querySelector("#city");
    const city = cityInput.value.trim();
    if (!city) return;

    subscribeToCity(city);
}

function subscribeToCity(city) {
    // 이미 구독 중인 도시인지 확인
    console.log(subscriptions);
    if (subscriptions[city]) {
        alert(`${city}는 이미 구독 중입니다.`);
        return;
    }

    if (!stompClient || stompClient.connected === false) {
        console.log('create SockJS /ws/weather');
        const socket = new SockJS('/ws/weather');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected.bind(null, city));
    } else {
        onConnected(city);
    }

    function onConnected(city) {
        // 도시 구독 설정
        const subscription = stompClient.subscribe(`/topic/weather/${city}`, (message) => {
            const weatherData = JSON.parse(message.body);

            // 온도 데이터 업데이트
            const temperatureCell = document.querySelector(`#temperature-${city}`);
            if (weatherData.error) {
                temperatureCell.innerHTML = `<span class="error-message">${weatherData.error}</span>`;
            } else {
                temperatureCell.innerHTML = `${weatherData.temperature}°C`;
            }

            // 구독 상태 저장
            subscriptions[city] = subscription;
        });
        // 서버에 도시 구독 요청
        stompClient.send("/app/weather", {}, city);
        console.log("send /app/weather/", city);

        // 구독한 도시를 로컬 스토리지에 저장
        const savedCities = JSON.parse(localStorage.getItem("subscribedCities")) || [];
        if (!savedCities.includes(city)) {
            savedCities.push(city);
            localStorage.setItem("subscribedCities", JSON.stringify(savedCities));
        }

        // 테이블에 도시 구독 컴포넌트 추가
        const tableBody = document.querySelector("#cityTable tbody");
        const newRow = createNewCityRow(city, tableBody.rows.length);
        // 새로운 행을 테이블에 추가
        tableBody.appendChild(newRow);
    }
}

function createNewCityRow(city, len) {
    const newRow = document.createElement('tr');
    const newIndex = len + 1;
    newRow.innerHTML = `
        <th scope="row">${newIndex}</th>
        <td>${city}</td>
        <td id="temperature-${city}">0°C</td>
        <td><button class="cancel-button" onclick="unsubscribeCity('${city}')">X</button></td>
        `;
    return newRow;
}

function unsubscribeCity(city) {
    if (subscriptions[city]) {
        // 구독 해제
        subscriptions[city].unsubscribe();
        delete subscriptions[city];

        // 해당 도시 행을 테이블에서 삭제
        const row = document.querySelector(`#temperature-${city}`).closest('tr');
        row.remove();

        // 로컬 스토리지에서 해당 도시 제거
        const savedCities = JSON.parse(localStorage.getItem("subscribedCities")) || [];
        const updatedCities = savedCities.filter(c => c !== city);
        localStorage.setItem("subscribedCities", JSON.stringify(updatedCities));

        alert(`${city} 구독이 해제되었습니다.`);
    }
}

async function searchCity() {
    const input = document.querySelector("#city");
    const cityDropdown = document.querySelector("#cityDropdown");
    const query = input.value.trim();

    if (query.length < 2) {
        // 입력 2자리 미만이면 드롭다운 숨김
        document.querySelector("#cityDropdown").style.display = 'none';
        return;
    }

    const response = await fetch(`/api/city?search=${query}`);
    const cities = await response.json();

    if (cities.length > 0) {
        cityDropdown.innerHTML = '';
        cities.forEach(city => {
            const item = document.createElement('div');
            item.classList.add('dropdown-item');
            item.textContent = `${city.country} ${city.state} ${city.name}`;
            item.onclick = () => selectCity(city);
            cityDropdown.appendChild(item);
        });
        cityDropdown.style.display = 'block'; // 드롭다운 보이기
    } else {
        cityDropdown.style.display = 'none'; // 데이터가 없으면 드롭다운 숨김
    }
}

function selectCity(city) {
    document.querySelector('#city').value = city.name;
    document.querySelector('#cityDropdown').style.display = 'none'; // 드롭다운 숨김
}

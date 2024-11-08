let stompClient;
let subscriptions = {};  // 구독을 관리할 객체

async function getWeather() {
    const cityInput = document.querySelector("#city");
    const city = cityInput.value.trim();
    if (!city) return;

    // 이미 구독 중인 도시인지 확인
    if (subscriptions[city]) {
        alert(`${city}는 이미 구독 중입니다.`);
        return;
    }

    const socket = new SockJS('/ws/weather');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        // 테이블에 도시 구독 컴포넌트 추가
        const tableBody = document.querySelector("#cityTable tbody");
        const newRow = createNewCityRow(city, tableBody.rows.length);
        // 새로운 행을 테이블에 추가
        tableBody.appendChild(newRow);

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
    });
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

        alert(`${city} 구독이 해제되었습니다.`);
    }
}

async function searchCity() {
    const cityInput = document.querySelector("#city");
    const searchQuery = cityInput.value.trim();

    if (searchQuery.length < 2) {
        document.getElementById("cityDropdown").style.display = 'none';
        return;
    }

    const response = await fetch(`/api/city?search=${searchQuery}`);
    const cities = await response.json();
    const dropdown = document.getElementById("cityDropdown");
    dropdown.innerHTML = '';

    if (cities.length > 0) {
        cities.forEach(city => {
            const cityDiv = document.createElement('div');
            cityDiv.textContent = city.country + ' ' + city.state + ' ' + city.name;
            cityDiv.onclick = () => {
                cityInput.value = city.name;
                dropdown.style.display = 'none';
            };
            dropdown.appendChild(cityDiv);
        });
        dropdown.style.display = 'block';
    } else {
        dropdown.style.display = 'none';
    }
}

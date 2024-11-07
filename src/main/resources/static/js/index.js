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
        // 도시 날씨 컴포넌트 생성
        const resultDiv = document.createElement('div');

        // 도시 구독 설정
        const subscription = stompClient.subscribe(`/topic/weather/${city}`, (message) => {
            const weatherData = JSON.parse(message.body);
            resultDiv.classList.add('weather-info');
            if (weatherData.error) {
                resultDiv.innerHTML = `<p class="error-message">Error: ${weatherData.error}</p>`;
            } else {
                resultDiv.innerHTML = `
                        <p>City: ${weatherData.name}</p>
                        <p>Temperature: ${weatherData.temperature}°C</p>
                    `;
            }

            // 구독 해제 버튼 추가
            const unsubscribeButton = document.createElement('button');
            unsubscribeButton.classList.add('cancel-button');
            unsubscribeButton.innerText = 'X';
            unsubscribeButton.onclick = () => unsubscribeCity(city, resultDiv);

            resultDiv.appendChild(unsubscribeButton);

            // 구독 상태 저장
            subscriptions[city] = subscription;
        });

        document.getElementById("cityList").appendChild(resultDiv);

        // 서버에 도시 구독 요청
        stompClient.send("/app/weather", {}, city);
    });
}

function unsubscribeCity(city, resultDiv) {
    if (subscriptions[city]) {
        subscriptions[city].unsubscribe();
        delete subscriptions[city];
        resultDiv.remove();
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

// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
$(document).ready(function(){
  $('.header').height($(window).height());

  $(".navbar a").click(function(){
    $("body,html").animate({
      scrollTop:$("#" + $(this).data('value')).offset().top
    },1000)
  
  })
})

function getComments() {
  let numCommentsPerPage = document.getElementById('num-comments').value;
  setupPagination(numCommentsPerPage).then(() => {
    fetch(`/list-comments?num-comments=${numCommentsPerPage}&page-num=${currentPage()}`)
    .then(response => response.json())
    .then((comments) => {
      const commentsListElement = document.getElementById('comments-container');
      commentsListElement.innerHTML = '';
      comments.forEach(comment => {
        commentsListElement.appendChild(createCommentElement(comment));
      });
    });
  });
}

async function setupPagination(numCommentsPerPage) {
  return fetch('/num-comments').then(response => response.text()).then((numCommentsString) => {
    let numComments = parseInt(numCommentsString);
    // default is one page
    let numPages = Math.max(Math.ceil(numComments / numCommentsPerPage), 1);
    const pageSelectElement = document.getElementById('page-num');
    
    if(pageSelectElement.options.length !== numPages) {
      updateOptions(pageSelectElement, numPages);
    }
  });
}

function updateOptions(pageSelectElement, numPages) {
  let savedPage = pageSelectElement.value;

  // remove old options first
  for (var i = pageSelectElement.options.length - 1; i >= 0; i--) {
    pageSelectElement.remove(i);
  }

  for(var i = 1; i <= numPages; i++) {
    const optionElement = document.createElement('option');
    optionElement.value = i;
    optionElement.innerText = `${i} of ${numPages}`;
    pageSelectElement.appendChild(optionElement);
  }
  pageSelectElement.value = savedPage <= numPages ? savedPage : numPages;
}

function currentPage() {
  return document.getElementById('page-num').value;
}

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(comment) {
  const commentElement = document.createElement('li');
  commentElement.className = 'row align-items-center';

  const profileElement = document.createElement('img');
  profileElement.className = 'account col-lg-1 col-md-2 col-sm-2';
  profileElement.src = comment.profile;

  const messageBoxElement = createMessageBoxElement(comment);
  const deleteButtonElement = createDeleteButtonElement();
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);
    commentElement.remove();
  });

  commentElement.appendChild(profileElement);
  commentElement.appendChild(messageBoxElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}

function createMessageBoxElement(comment) {
  const messageBoxElement = document.createElement('div');
  messageBoxElement.className = 'col-lg-10 col-md-8 col-sm-8';
  const nameElement = document.createElement('p');
  nameElement.className = 'name';
  nameElement.innerText = comment.name;
  const messageElement = document.createElement('p');
  messageElement.className = 'message';
  messageElement.innerText = comment.message;
  messageBoxElement.appendChild(nameElement);
  messageBoxElement.appendChild(messageElement);
  return messageBoxElement;
}

function createDeleteButtonElement() {
  const deleteButtonElement = document.createElement('button');
  const deleteIconElement = document.createElement('i');
  deleteIconElement.className = 'material-icons delete col-lg-1 col-md-2 col-sm-2';
  deleteIconElement.innerText = 'close';
  deleteButtonElement.appendChild(deleteIconElement);
  return deleteButtonElement;
}

/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comment', {method: 'POST', body: params}).then(getComments());
}

function onSignIn(googleUser) {
  var profile = googleUser.getBasicProfile();
  document.getElementById('name').innerText = profile.getName();
  document.getElementById('profile').src = profile.getImageUrl();
  document.getElementById('new-comment-form').style.display = "block";
  document.getElementById('signin-section').style.display = "none";
  document.getElementById('signout').style.display = "block";
}

function onNewComment() {
  const params = new URLSearchParams();
  params.append('name', document.getElementById('name').innerText);
  params.append('profile', document.getElementById('profile').src);
  let newCommentElement = document.getElementById('text-input');
  params.append('text-input', newCommentElement.value);
  fetch('/new-comment', {method: 'POST', body: params}).then(getComments());
  newCommentElement.value = '';
  window.location = "#comments";
}

function signOut() {
  var auth2 = gapi.auth2.getAuthInstance();
  auth2.signOut().then(function () {
    document.getElementById('new-comment-form').style.display = "none";
    document.getElementById('signin-section').style.display = "block";
    window.location = "#comments";
  });
}

function initMap() {
  var mapStyle = [{
    'featureType': 'all',
    'elementType': 'all',
    'stylers': [{'visibility': 'off'}]
  }, {
    'featureType': 'landscape',
    'elementType': 'geometry',
    'stylers': [{'visibility': 'on'}, {'color': '#fcfcfc'}]
  }, {
    'featureType': 'water',
    'elementType': 'labels',
    'stylers': [{'visibility': 'off'}]
  }, {
    'featureType': 'water',
    'elementType': 'geometry',
    'stylers': [{'visibility': 'on'}, {'hue': '#5f94ff'}, {'lightness': 60}]
  }];

  let map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 40, lng: -100},
    zoom: 5,
    styles: mapStyle
  });

  loadMapShapes(map);
  // set up the style rules and events for google.maps.Data
  map.data.addListener('mouseover', mouseInToRegion);
  map.data.addListener('mouseout', mouseOutOfRegion);

}

/** Loads the state boundary polygons from a GeoJSON source. */
function loadMapShapes(map) {
  // load US state outline polygons from a GeoJSON file
  map.data.loadGeoJson('https://storage.googleapis.com/mapsdevsite/json/states.js', { idPropertyName: 'gx_id' });
  // wait for the request to complete by listening for the first feature to be added
  google.maps.event.addListenerOnce(map.data, 'addfeature', function() {
    loadDessertVotesData(map).then(map.data.setStyle(setMapStyle));
  });

}

async function loadDessertVotesData(map) {
  return fetch('/list-state-dessert-votes').then(response => response.json()).then(statesData => {
    statesData.forEach(stateData => {
      let feature = map.data.getFeatureById(stateData.state);
      feature.setProperty('cheesecake-votes', stateData.cheesecakeVotes)
      feature.setProperty('apple-pie-votes', stateData.applePieVotes)
      feature.setProperty('chocolate-chip-cookies-votes', stateData.chocolateChipCookiesVotes)
      feature.setProperty('tiramisu-votes', stateData.tiramisuVotes)
      feature.setProperty('chocolate-cake-votes', stateData.chocolateCakeVotes);
    });
  });
}

function setMapStyle(feature) {
  var dessertCategories = 
  [
    'cheesecake-votes', 
    'apple-pie-votes', 
    'chocolate-chip-cookies-votes', 
    'tiramisu-votes', 
    'chocolate-cake-votes'
  ]

  var colors = ['#f1ded7', '#c3c9d1', '#c9bcc9', '#ceafb5', '#c0dadb'];

  var dessertVotes = dessertCategories.map(dessert => feature.getProperty(dessert));
  var maxIndex = indexOfMax(dessertVotes);
  // color gray if the state has no votes
  var stateColor = dessertVotes[maxIndex] === 0 ? '#888' : colors[maxIndex];

  var outlineWeight = 0.5, zIndex = 1;
  if (feature.getProperty('state') === 'hover') {
    outlineWeight = zIndex = 2;
  }

  return {
    strokeWeight: outlineWeight,
    strokeColor: '#fff',
    zIndex: zIndex,
    fillColor: stateColor,
    fillOpacity: 0.75,
  };
}

function indexOfMax(arr) {
    if (arr.length === 0) {
        return -1;
    }

    var max = arr[0];
    var maxIndex = 0;

    for (var i = 1; i < arr.length; i++) {
        if (arr[i] > max) {
            maxIndex = i;
            max = arr[i];
        }
    }

    return maxIndex;
}

/**
  * Responds to the mouse-in event on a map shape (state).
  *
  * @param {?google.maps.MouseEvent} e
  */
function mouseInToRegion(e) {
  // set the hover state so the setStyle function can change the border
  e.feature.setProperty('state', 'hover');

  // update the label
  document.getElementById('data-label').textContent = e.feature.getProperty('NAME');
  document.getElementById('cheesecake-value').textContent = 
    `Cheesecake: ${e.feature.getProperty('cheesecake-votes').toLocaleString()}`;
  document.getElementById('apple-pie-value').textContent = 
    `Apple Pie: ${e.feature.getProperty('apple-pie-votes').toLocaleString()}`;
  document.getElementById('chocolate-chip-cookies-value').textContent = 
    `Chocolate Chip Cookies: ${e.feature.getProperty('chocolate-chip-cookies-votes').toLocaleString()}`;
  document.getElementById('tiramisu-value').textContent = 
    `Tiramisu: ${e.feature.getProperty('tiramisu-votes').toLocaleString()}`;
  document.getElementById('chocolate-cake-value').textContent = 
    `Chocolate Cake: ${e.feature.getProperty('chocolate-cake-votes').toLocaleString()}`;
  document.getElementById('data-box').style.display = 'block';
}


/**
  * Responds to the mouse-out event on a map shape (state).
  *
  * @param {?google.maps.MouseEvent} e
  */
function mouseOutOfRegion(e) {
  // reset the hover state, returning the border to normal
  e.feature.setProperty('state', 'normal');
}

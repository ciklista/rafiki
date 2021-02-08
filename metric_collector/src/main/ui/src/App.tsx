import React from 'react';
import './App.css';
import Header from './Header/Header';
import Home from './Home/Home';
import Server from './Server/Server';
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";

function App() {
  return (
    <div className="App">
      <Router>
        <Header />
        <Switch>
          <Route exact path="/">
            <Home />
          </Route>
          <Route path="/server/:id">
            <Server />
          </Route>
        </Switch>
      </Router>
    </div>
  );
}

export default App;

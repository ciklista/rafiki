import React from 'react';
import './App.css';
import Header from './Header';
import Home from './Home';
import Experiment from './Experiment';
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";

function App() {
  return (
    <div className="bg-gray-100 min-h-screen flex flex-col">
      <Router>
        <Header />
        <Switch>
          <Route exact path="/">
            <Home />
          </Route>
          <Route path="/experiment/:jar_id">
            <Experiment />
          </Route>
        </Switch>
      </Router>
    </div>
  );
}

export default App;

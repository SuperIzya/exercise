import React from 'react';
import style from './app.scss';
import DevTools from "mobx-react-devtools";
import { Watchers, WatchersList } from './components/watchers';

const list = new WatchersList();

const App = () => (
  <div className={style.container}>
    <Watchers list={list}/>
    <DevTools/>
  </div>
);

export default App;

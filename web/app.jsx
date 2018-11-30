import React from 'react';
import style from './app.scss';
import DevTools from "mobx-react-devtools";
import { Watchers, WatchersList } from './components/watchers';
import Header from './components/header';

const list = new WatchersList();

const App = () => (
  <div className={style.container}>
    <div className={style.header}>
      <Header/>
    </div>
    <div className={style.content}>
      <Watchers list={list}/>
      <DevTools/>
    </div>
  </div>
);

export default App;

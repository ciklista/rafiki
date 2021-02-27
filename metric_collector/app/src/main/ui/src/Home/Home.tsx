import axios from 'axios';
import Job from '../models/Job';
import React, { useEffect } from 'react';
import './Home.css';
import { Link } from 'react-router-dom';
import AddJob from '../AddJob';
import useLocalState from '../useLocalState';

export default function Home() {

    const [servers, setServers] = useLocalState([], 'job-list');

    return (
        <div className="content">
            <AddJob></AddJob>
            {servers.map((server: Job) =>
                <Link to={`/server/${server.ip_adress}`} className='server transition duration-200 opacity-100 hover:opacity-80' key={server.jar_id}>
                    <div>{server.name}</div>
                    <div>{server.ip_adress}</div>
                </Link>
            )}
        </div>
    )
}
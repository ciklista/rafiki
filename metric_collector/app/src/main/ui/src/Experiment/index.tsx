import React, {useRef, useState} from 'react';
import { useParams } from 'react-router-dom';
import JobGraph from '../Experiment-Graph';
import './Experiment.css';

export default function Server() {
    let { jar_id } = useParams<{ jar_id: string }>();
    const position = { x: 0, y: 0 };
    const edgeType = 'solid';
    const initialElements = [
        {
            id: '4',
            data: { label: 'node 4' },
            position,
        },
        {
            id: '5',
            data: { label: 'node 5' },
            position,
        },
        {
            id: '6',
            data: { label: 'output' },
            position,
        },
        { id: '7', type: 'output', data: { label: 'output' }, position },
        { id: 'e45', source: '4', target: '5', type: edgeType},
        { id: 'e56', source: '5', target: '6', type: edgeType},
        { id: 'e57', source: '5', target: '7', type: edgeType},
    ];
    const [elements, setElements] = useState(initialElements);
    const configInput = useRef(null);

    function updateNode(): void {
        setElements((elements: any) =>
            elements.map((el: any) => {
                if (el.id == '1') {
                    el.data = {
                        ...el.data,
                        label: "75000m/s\r\n 40% Capacity"
                    }
                }
                return el;
            })
        );
    }

    function setNewParallelism(e: any) {
        e.preventDefault();
        if (!configInput || !configInput.current) return;
        // @ts-ignore
        const config = configInput.current.value;
        const operatorParallelism = config.split(',');
        setElements(generateGraphElements(operatorParallelism));
    }

    function generateGraphElements(operatorParallelism: string[]): any {
        return operatorParallelism.flatMap((operator: string, index: number, array) =>
            [...Array(parseInt(operator)).keys()].flatMap((suboperator:number) => {
                let nodesAndEdges: any[] = [];
                if (index > 0) {
                    nodesAndEdges.push(...[...Array(parseInt(array[index-1])).keys()].map((prevOperator: number) => {
                        return {
                            id: 'e' + index + suboperator.toString() + (index-1).toString() + prevOperator.toString(),
                            type: edgeType,
                            target: index + suboperator.toString(),
                            source: (index - 1).toString() + prevOperator.toString()
                        }
                    }));
                }
                return [...nodesAndEdges, {
                    position,
                    id: index + suboperator.toString(),
                    data: {
                        label: `Node ${index + suboperator.toString()}`
                    }
                }];
            })
        );
    }

    return (
        <div className="flex-1">
            {/*<div>{server?.name}</div>*/}
            <div className="h-96">
                <form className="mr-0 ml-auto w-max bg-gray-100 rounded shadow-md p-2 mt-1" onSubmit={setNewParallelism}>
                    <input type="text" className="bg-gray-200 shadow-inner z-0 p-1" ref={configInput} placeholder="New parallelism config csv" required minLength={1}/>
                    <input onClick={setNewParallelism} className="p-1 bg-blue-300 rounded ml-1 w-8" value="Set" type="submit"/>
                </form>
                <JobGraph initialElements={elements} test={"Test"} />
            </div>
        </div>
    )
}
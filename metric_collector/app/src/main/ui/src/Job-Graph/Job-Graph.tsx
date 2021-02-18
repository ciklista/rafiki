import React, { useEffect, useState } from 'react';
import Graph from "react-vis-network-graph"

function JobGraph() {
    const [graph, setGraph] = useState<any>();
    const [options, setOptions] = useState<any>();
    useEffect(() => {
        let isMounted = true;

        if (isMounted) {

            setGraph({});
            setGraph({
                nodes: [
                ],
                edges: [
                ]
            });
            setOptions({});
            setOptions(
                {
                    layout: {
                        hierarchical: true,
                    },
                    edges: {
                        color: "#000000"
                    },
                    height: "500px",
                    interaction: {
                        dragNodes: false,
                        dragView: false
                    },
                    physics: false
                }
            )
        }

        return () => { isMounted = false };

    }, [])

    return (
        <Graph
            graph={graph}
            options={options}
        />
    )
}

export default JobGraph;
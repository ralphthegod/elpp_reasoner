import matplotlib.pyplot as plt


def read_performance_data(file_path):
    with open(file_path, 'r') as file:
        lines = file.readlines()
    
    performance_data = {}
    reasoners = []
    current_ontology = None
    
    for line in lines:
        if not line.strip():  
            continue
        
        if not line.startswith(' ') and not line.startswith('['):  
            current_ontology = line.strip()
            performance_data[current_ontology] = []
        
        elif '[' in line:  
            reasoner_name = line.split('[')[1].split(']')[0].strip()
            time_value = float(line.split(':')[1].replace('s', '').strip())
            
            if reasoner_name not in reasoners:
                reasoners.append(reasoner_name)
            
            performance_data[current_ontology].append((reasoner_name, time_value))
    
    return performance_data, reasoners


def prepare_data_for_plotting(performance_data, reasoners):
    plot_data = {}
    
    for ontology, results in performance_data.items():
        times = []
        for reasoner in reasoners:
            
            time = next((time for r, time in results if r == reasoner), None)
            times.append(time if time is not None else 0)  
        plot_data[ontology] = times
    
    return plot_data


file_path = r'INSERT PATH'


performance_data, reasoners = read_performance_data(file_path)
plot_data = prepare_data_for_plotting(performance_data, reasoners)


for ontology, times in plot_data.items():
    plt.figure(figsize=(10, 6))
    plt.bar(reasoners, times, color='skyblue')
    plt.title(f'Tempi di esecuzione per {ontology}')
    plt.ylabel('Tempo (s)')
    plt.xlabel('Reasoner')
    plt.show()

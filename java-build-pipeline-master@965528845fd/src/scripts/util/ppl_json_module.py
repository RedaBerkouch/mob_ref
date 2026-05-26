# ppl_json_module.py

def to_json(dictionary):
    def serialize_value(value):
        if isinstance(value, set):
            return list(value)  # Convert set to list
        elif hasattr(value, 'to_dict'):
            return value.to_dict()  # Serialize if it's an object with a to_dict method
        elif isinstance(value, dict):
            return to_json(value)  # Recursively serialize nested dictionaries
        else:
            return value  # Return value as is if it's serializable

    return {
        key: serialize_value(value)
        for key, value in dictionary.items() if value is not None
    }

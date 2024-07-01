# Copyright (C) 2024 Seungkyun Kim. All rights reserved.
#
# SPDX-License-Identifier: MIT
#

"""
https://yaml.org/spec/1.2.2/

yaml_string = '''
key1: value1
key2: value2
nested_key:
    nested_value1: 10
    nested_value2: 20
'''

# Example usage1
import pprint
with open("pubspec.lock", "r") as f:
  parser = SimpleYAMLParser()
  pubspec = parser.load(f)
pretty_json_str = pprint.pformat(pubspec, compact=True).replace("'",'"')
print(pretty_json_str)
"""

class SimpleYAMLParser:
    def parse(self, text):
        """
        Parse YAML string and return the Python data structure.
        """
        lines = text.strip().split('\n')
        parsed_data, _ = self._parse_lines(lines)
        return parsed_data

    def load(self, file):
        yaml_string = file.read()
        return self.parse(yaml_string)

    def _parse_lines(self, lines, level=0):
        obj = None
        i = 0
        last_level = level

        while i < len(lines):
            line = lines[i].rstrip()
            i += 1
            if not line or line.startswith('#'):
                continue  # Skip empty lines and comments

            current_level = self._get_indent_level(line)

            if current_level < level or current_level < last_level:
                return obj, i - 1  # Return to the previous level

            last_level = current_level
            line_content = line.lstrip()

            if line_content.startswith('-'):
                next_level = self._get_indent_level(line, isArray = True)
                i -= 1; # feed current line again
                lines[i] = line.replace('-', ' ', 1)
                nested, used_lines = self._parse_lines(lines[i:], level=next_level)

                if obj is None:
                    obj = []
                elif not isinstance(obj, list):
                    raise RuntimeError("Invalid yaml format")

                obj.append(nested)
                i += used_lines

            elif ':' in line_content:
                if obj is None:
                    obj = {}
                elif not isinstance(obj, dict):
                    raise RuntimeError("Invalid yaml format")

                key, value = line_content.split(':', 1)
                if value:
                    obj[key] = self._parse_value(value)
                else:
                    nested, used_lines = self._parse_lines(lines[i:], level=current_level)
                    obj[key] = nested
                    i += used_lines
            else:
                return self._parse_value(line_content), i
        return obj, i


    def _parse_value(self, value):
        value = value.strip()

        value = self.__parse_value(value)
        if type(value) == str:
          return value.strip()

        return value

    def __parse_value(self, value):
        if value.startswith('"') and value.endswith('"'):
            return value[1:-1]
        elif value.startswith("'") and value.endswith("'"):
            return value[1:-1]
        elif value.isdigit():
            return int(value)
        elif value.replace('.', '', 1).isdigit():
            return float(value)
        elif value.lower() in ['true', 'false']:
            return value.lower() == 'true'
        else:
            return value

    @staticmethod
    def _get_indent_level(line, isArray=False):
        escape_chars = ' '
        if isArray:
            escape_chars += '-'
        return len(line) - len(line.lstrip(escape_chars))

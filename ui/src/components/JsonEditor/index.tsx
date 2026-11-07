import { useMemo } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { json } from '@codemirror/lang-json';

interface IProps {
  children?: import('react').ReactNode;
  value?: string;
  disabled?: boolean;
  onChange?(value: string): void;
}

const JsonEditor = (props: IProps) => {
  const { value, disabled, onChange } = props;

  const editorValue = useMemo(() => {
    try {
      if (value) {
        return JSON.stringify(JSON.parse(value), null, 2);
      }
    } catch {
      return value || '';
    }
    return '';
  }, [value]);

  return (
    <CodeMirror
      value={editorValue}
      extensions={[json()]}
      readOnly={disabled}
      basicSetup={{
        tabSize: 2,
        highlightActiveLine: !disabled,
        foldGutter: true,
      }}
      onChange={(nextValue) => onChange?.(nextValue)}
    />
  );
};

export default JsonEditor;

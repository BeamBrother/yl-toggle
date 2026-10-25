import { render } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import { FieldErrorsImpl, useForm } from 'react-hook-form';
import { IntlWrapper } from 'components/utils/wrapper';
import FormItem from '.';

it('Key snapshot', async () => {
  await (async () => {
    const { asFragment, rerender } = render(<FormItem label={'test label'}>test</FormItem>, {
      wrapper: IntlWrapper,
    });

    expect(asFragment()).toMatchSnapshot();

    rerender(
      <FormItem
        error={{ message: 'error message' }}
        label={'test label'}
        errorCss={{
          color: 'pink'
        }}
        required={true}
      />
    );

    expect(asFragment()).toMatchSnapshot();
  })();
});
